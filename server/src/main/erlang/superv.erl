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
  {ok, {{one_for_one, 2, 1}, [
    {chSupV, {guardianAngelSuperv, spawn, []}, transient, 2000, supervisor, [guardianAngelSuperv]},
    {gG, {goodGod, spawn, []}, transient, 2000, worker, [goodGod]}
  ]}}.


