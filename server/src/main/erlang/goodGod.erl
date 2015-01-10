%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 01. Dec 2014 9:28 PM
%%%-------------------------------------------------------------------
-module(goodGod).
-author("blueeyedhush").
-include("../include/global.hrl").
%% API
-export([spawn/0, start/0, loop/1, inf_clientConn/0, inf_nodeCreated/1, inf_clientDisconn/0, req_content/0, req_id_range/0]).

spawn() ->
  Pid = erlang:spawn_link(?MODULE, start, []),
  {ok, Pid}.

start() ->
  info_msg("[gG] Started"),
  register(goodGod, self()),
  process_flag(trap_exit, true),
  {ok, Port} = application:get_env(port),
  {ok, LS} = gen_tcp:listen(Port, [{active, true}, list]),
  io:format("~w \n", [LS]),
  %spawn first child
  FirstChildPID = erlang:spawn(guardianAngel, start, [LS]),
  register(child, FirstChildPID),
  {ok, IdPoolSize }= application:get_env(idPoolSize),
  %io:format("~p \n", [IdPoolSize]),
  loop(#state{listenSocket = LS, clientList = [FirstChildPID], nodeList = [], firstFreeId = -2147483648, idPoolSize = IdPoolSize}).

loop(State) ->
  receive
    {clientConnected, PID} ->
      info_msg("[gG] Client connected"),
      erlang:spawn(guardianAngel, start, [State#state.listenSocket]),
      goodGod:loop(State#state{clientList = [PID|State#state.clientList]});
    {clientDisconnected, PID} ->
      info_msg("[gG] Client disconnected"),
      goodGod:loop(State#state{clientList = remove_from_list(PID, State#state.clientList, [])});
    {nodeCreated, PID, Descriptor} ->
      info_msg("[gG] New node created with coords: " ++ integer_to_list(Descriptor#nodeCreated.x) ++ " " ++ integer_to_list(Descriptor#nodeCreated.y)),
      NewState = State#state{nodeList = [#node{posX = Descriptor#nodeCreated.x, posY = Descriptor#nodeCreated.y}|State#state.nodeList]},
      % @ToDo: Just a temporary fix, rewrite it
      broadcast_to_all_but(Descriptor#nodeCreated{type = <<"NodeCreatedContent">>}, PID, State#state.clientList),
      goodGod:loop(NewState);
    {reqContent, PID} ->
      send_content(PID, State),
      goodGod:loop(State);
    {reqIdRange, PID} ->
      if
        State#state.firstFreeId > 2147483647 - State#state.idPoolSize ->
          erlang:error(idRangeExhausted);
        true ->
          io:format("\nAssigned pool, firstId: ~p\n", [State#state.firstFreeId]),
          Last = State#state.firstFreeId + State#state.idPoolSize - 1,
          send_id_pool(PID, State#state.firstFreeId, Last),
          goodGod:loop(State#state{firstFreeId = Last + 1})
      end;
    {'EXIT', _, _} ->
      terminate(State);
    A ->
      info_msg("[gG] Unexpected message has arrived: ~p", [A]),
      goodGod:loop(State)
  end.

terminate(State) ->
  info_msg("[gG] Terminating..."),
  gen_tcp:close(State#state.listenSocket).

broadcast_to_all_but(_, _, []) -> ok;
broadcast_to_all_but(Record, But, [R|Recipients]) when R == But ->
  broadcast_to_all_but(Record, But, Recipients);
broadcast_to_all_but(Record, But, [R|Recipients]) when R /= But ->
  send_retransmit(R, Record),
  broadcast_to_all_but(Record, But, Recipients).

remove_from_list(El, [], Acc) ->
  lists:reverse(Acc);
remove_from_list(El, [A|List], Acc) when El == A ->
  remove_from_list(El, List, Acc);
remove_from_list(El, [A|List], Acc) ->
  remove_from_list(El, List, [A|Acc]).

% wrappers for message-based communication with goodGod
inf_clientConn() ->
  goodGod ! {clientConnected, self()}.

inf_clientDisconn() ->
  goodGod ! {clientDisconnected, self()}.

inf_nodeCreated(Descriptor) ->
  goodGod ! {nodeCreated, self(), Descriptor}.

req_content() ->
  goodGod ! {reqContent, self()}.

req_id_range() ->
  goodGod ! {reqIdRange, self()}.

send_retransmit(PID, Mesg) ->
  PID ! {gG, retrans, Mesg}.

send_content(PID, State) ->
  PID ! {gG, content, State#state.nodeList}.

send_id_pool(PID, First, Last) ->
  PID ! {gG, idPool, {First, Last}}.