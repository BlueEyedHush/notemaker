%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 27. Nov 2014 9:31 AM
%%%-------------------------------------------------------------------
-module(superv).
-behaviour(supervisor).
-include("../include/global.hrl").
-author("blueeyedhush").

%% API
-export([
  init/1
]).

init(_Arg) ->
  info_msg("Supervisor started"),
  {ok, {{one_for_one, 3, 10}, [
    {gG, {goodGod, spawn, []}, permanent, 10000, worker, dynamic}
  ]}}.


