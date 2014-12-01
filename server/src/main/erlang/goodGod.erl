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
-export([spawn/0, start/0, loop/0]).

spawn() ->
  Pid = erlang:spawn_link(?MODULE, start, []),
  {ok, Pid}.

start() ->
  io:format("gG started!"),
  loop().

loop() ->
  timer:sleep(10000),
  goodGod:loop().