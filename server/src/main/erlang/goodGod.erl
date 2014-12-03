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
-export([spawn/0, start/0, loop/1]).

spawn() ->
  Pid = erlang:spawn_link(?MODULE, start, []),
  {ok, Pid}.

start() ->
  info_msg("gG started"),
  register(goodGod, self()),
  {ok, Port} = application:get_env(port),
  {ok, LS} = gen_tcp:listen(Port, [{active, true}, list]),
  info_msg("Listening started\n"),
  loop(LS).

loop(ListenSocket) ->
  erlang:spawn(guardianAngel, start, [ListenSocket]),
  receive
    clientConnected ->
      info_msg("Somebody has connected to the client. Spawning a new one");
    A ->
      info_msg("Unexpected message has arrived: ~p", [A])
  end,
  goodGod:loop(ListenSocket).