%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2015, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 14. Jan 2015 5:37 PM
%%%-------------------------------------------------------------------
-module(database).
-author("blueeyedhush").

-include("../include/global.hrl").

%% API
-export([start/0, shutdown/0]).

start() ->
  SchemeLoc = application:get_env(dbLoc),
  application:load(mnesia),
  application:set_env(mnesia, dir, SchemeLoc),
  case mnesia:create_schema([node()]) of
    {error, Reason} ->
      warning_msg(<<"Schema creation failed. Probably already exists?">>),
      io:format("\n ~p \n", [Reason]);
    _ -> info_msg(<<"Schema successfully created">>)
  end,
  application:start(mnesia),
  case mnesia:create_table(node, [{attributes, record_info(fields, node)}, {type, set}]) of
    {atomic, ok} -> info_msg(<<"Created node table">>);
    {aborted, {already_exists, T}} -> info_msg(<<"Table node already exists.">>);
    {aborted, Reason1} ->
      error_msg(<<"Table creation failed!">>),
      io:format("\n ~p \n", [Reason1]),
      exit(normal)
  end,
  mnesia:wait_for_tables([node], 3000).

shutdown() ->
  application:stop(mnesia).