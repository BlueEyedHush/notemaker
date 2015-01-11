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
-export([spawn/0, start/0, loop/1, inf_clientConn/0, inf_nodeCreated/1, inf_clientDisconn/0, req_content/0, req_id_range/0, inf_nodeMoved/1, inf_nodeDeleted/1, inf_textSend/1]).

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
      NewState = State#state{nodeList = [#node{id = Descriptor#nodeCreated.id, posX = Descriptor#nodeCreated.x, posY = Descriptor#nodeCreated.y}|State#state.nodeList]},
      % @ToDo: Just a temporary fix, rewrite it
      broadcast_to_all_but(Descriptor#nodeCreated{type = <<"NodeCreatedContent">>}, PID, NewState#state.clientList),
      goodGod:loop(NewState);
    {nodeMoved, PID, Descriptor} ->
      info_msg("[gG] Node wants to be moved"),
      Res = find_single_matching_record(
        fun
          (P) when P#node.id == Descriptor#nodeMoved.id -> true;
          (_) -> false
        end, State#state.nodeList, []),
      case Res of
        {notFound, _} ->
          warning_msg(<<"Tried to move node which is not present">>),
          exit(nodeNotPresent);
        {N, ListWithoutEl} when is_record(N, node) ->
          ModNode = N#node{posX = Descriptor#nodeMoved.x, posY = Descriptor#nodeMoved.y},
          NewState = State#state{nodeList = [ModNode|ListWithoutEl]},
          broadcast_to_all_but(Descriptor#nodeMoved{type = <<"NodeMovedContent">>}, PID, NewState#state.clientList),
          goodGod:loop(NewState)
      end;
    {nodeDel, PID, Descriptor} ->
      info_msg("[gG] Node wants to be deleted"),
      Res = find_single_matching_record(
        fun
          (P) when P#node.id == Descriptor#nodeDeleted.id -> true;
          (_) -> false
        end, State#state.nodeList, []),
      case Res of
        {notFound, _} ->
          warning_msg(<<"Tried to delete node which is not present">>),
          exit(nodeNotPresent);
        {_, ListWithoutEl} ->
          NewState = State#state{nodeList = ListWithoutEl},
          broadcast_to_all_but(Descriptor#nodeDeleted{type = <<"NodeDeletedContent">>}, PID, NewState#state.clientList),
          goodGod:loop(NewState)
      end;
    {textSend, PID, Descriptor} ->
      info_msg("[gG] Node text is being changed"),
      Res = find_single_matching_record(
        fun
          (P) when P#node.id == Descriptor#textSending.id -> true;
          (_) -> false
        end, State#state.nodeList, []),
      case Res of
        {notFound, _} ->
          warning_msg(<<"Tried to move node which is not present">>),
          exit(nodeNotPresent);
        {N, ListWithoutEl} when is_record(N, node) ->
          ModNode = N#node{text = Descriptor#textSending.text},
          NewState = State#state{nodeList = [ModNode|ListWithoutEl]},
          broadcast_to_all_but(Descriptor#textSending{type = <<"NodeMessageContent">>}, PID, NewState#state.clientList),
          goodGod:loop(NewState)
      end;
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

%also removed that element from the list, returns {Element, Rest}, changes order of element in the list
find_single_matching_record(_, [], Rest) ->
  {notFound, Rest};
find_single_matching_record(Pred, [H|T], AlreadyChecked) ->
  Res = Pred(H),
  if
    Res == true ->
      {H, AlreadyChecked ++ T};
    Res == false ->
      find_single_matching_record(Pred, T, [H|AlreadyChecked])
  end.

% wrappers for message-based communication with goodGod
inf_clientConn() ->
  goodGod ! {clientConnected, self()}.

inf_clientDisconn() ->
  goodGod ! {clientDisconnected, self()}.

inf_nodeCreated(Descriptor) ->
  goodGod ! {nodeCreated, self(), Descriptor}.

inf_nodeMoved(Descriptor) ->
  goodGod ! {nodeMoved, self(), Descriptor}.

inf_nodeDeleted(Desc) ->
  goodGod ! {nodeDel, self(), Desc}.

inf_textSend(Desc) ->
  goodGod ! {textSend, self(), Desc}.

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