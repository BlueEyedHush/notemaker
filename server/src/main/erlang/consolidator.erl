%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 27. Nov 2014 9:31 AM
%%%-------------------------------------------------------------------
-module(consolidator).
-behaviour(supervisor).
-author("blueeyedhush").

%% API
-export([
  init/1
]).

init(Arg) ->
  io:format("Supervisor started"),
  {ok, {{one_for_one, 3, 10}, []}}.


