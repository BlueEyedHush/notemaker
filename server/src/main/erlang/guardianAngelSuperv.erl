%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2015, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 14. Feb 2015 9:50 PM
%%%-------------------------------------------------------------------
-module(guardianAngelSuperv).
-behaviour(supervisor).
-include("../include/global.hrl").
-author("blueeyedhush").

%% API
-export([spawn/0, init/1, addChild/1]).

spawn() ->
  supervisor:start_link({local, gaSuperV}, ?MODULE, []).

init(_Arg) ->
  info_msg("ChildSuperV started"),
  {ok, {{simple_one_for_one, 2, 1}, [
    {ga, {guardianAngel, spawn, []}, temporary, 200, worker, [guardianAngel]}
  ]}}.

addChild(ListenSocket) ->
  supervisor:start_child(gaSuperV, [ListenSocket]).