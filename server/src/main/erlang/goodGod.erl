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
-export([spawn/0, start/0, loop/2, inf_clientConn/0, inf_nodeCreated/1, inf_clientDisconn/0]).

spawn() ->
  Pid = erlang:spawn_link(?MODULE, start, []),
  {ok, Pid}.

start() ->
  info_msg("gG started"),
  register(goodGod, self()),
  {ok, Port} = application:get_env(port),
  {ok, LS} = gen_tcp:listen(Port, [{active, true}, list]),
  loop(LS, []).

loop(ListenSocket, ClientsList) ->
  erlang:spawn(guardianAngel, start, [ListenSocket]),
  receive
    {clientConnected, PID} ->
      goodGod:loop(ListenSocket, [PID|ClientsList]);
    {clientDisconnected, PID} ->
      goodGod:loop(ListenSocket, remove_from_list(PID, ClientsList, []));
    {nodeCreated, PID, Descriptor} ->
      info_msg("New node created with coords: " ++ integer_to_list(Descriptor#nodeCreated.x) ++ " " ++ integer_to_list(Descriptor#nodeCreated.y)),
      broadcast_to_all_but(Descriptor, PID, ClientsList),
      %broadcast_to_all(Descriptor, ClientsList),
      goodGod:loop(ListenSocket, ClientsList);
    A ->
      info_msg("Unexpected message has arrived: ~p", [A]),
      goodGod:loop(ListenSocket, ClientsList)
  end.

broadcast_to_all(_, []) -> ok;
broadcast_to_all(Record, [R|Recipients]) ->
  R ! {gG, Record},
  broadcast_to_all(Record, Recipients).

broadcast_to_all_but(_, _, []) -> ok;
broadcast_to_all_but(Record, But, [R|Recipients]) when R == But ->
  broadcast_to_all_but(Record, But, Recipients);
broadcast_to_all_but(Record, But, [R|Recipients]) when R /= But ->
  R ! {gG, Record},
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