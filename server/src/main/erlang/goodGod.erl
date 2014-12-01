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

%% API
-export([spawn/0, start/0, loop/1]).

spawn() ->
  Pid = erlang:spawn_link(?MODULE, start, []),
  {ok, Pid}.

start() ->
  io:format("gG started!\n"),
  {ok, Port} = application:get_env(port),
  {ok, LS} = gen_tcp:listen(Port, [{active, true}, binary]),
  io:format("Listening started \n"),
  loop(LS).

loop(ListenSocket) ->
  {ok, AS} = gen_tcp:accept(ListenSocket),
  erlang:spawn(guardianAngel, start, [AS]),
  goodGod:loop().