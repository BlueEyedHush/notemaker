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
-include_lib("stdlib/include/ms_transform.hrl").

%% API
-export([start/0, shutdown/0, createNode/1, deleteNode/1, updateNode/2, getNodeById/1,
  getAllNodes/0, getAll/1, getOrCreateConfig/2, updateFFID/1]).

% Initialization

start() ->
  {ok, SchemeLoc} = application:get_env(dbLoc),
  application:load(mnesia),
  application:set_env(mnesia, dir, SchemeLoc),
  case mnesia:create_schema([node()]) of
    {error, Reason} ->
      warning_msg(<<"Schema creation failed. Probably already exists?">>),
      io:format("\n ~p \n", [Reason]);
    _ -> info_msg(<<"Schema successfully created">>)
  end,
  application:ensure_started(mnesia),
  case mnesia:create_table(node, [{attributes, record_info(fields, node)}, {type, set}]) of
    {atomic, ok} -> info_msg(<<"Created node table">>);
    {aborted, {already_exists, _}} -> info_msg(<<"Table node already exists.">>);
    {aborted, Reason1} ->
      error_msg(<<"node table creation failed!">>),
      io:format("\n ~p \n", [Reason1]),
      exit(normal)
  end,
  case mnesia:create_table(config, [{attributes, record_info(fields, config)}, {type, set}]) of
    {atomic, ok} -> info_msg(<<"Created config table">>);
    {aborted, {already_exists, _}} -> info_msg(<<"Table config already exists.">>);
    {aborted, Reason2} ->
      error_msg(<<"config table creation failed!">>),
      io:format("\n ~p \n", [Reason2]),
      exit(normal)
  end,
  mnesia:wait_for_tables([node], 3000).

shutdown() -> ok.
  % @ToDo: For some reason, this takes veery long, causing supervisor to be killed
  %application:stop(mnesia).

% Data Manipulation
createNode(Node) when is_record(Node, node) ->
  mnesia:activity(async_dirty,
    fun() ->
      mnesia:write(Node)
    end
  ).

deleteNode(NodeId) when is_integer(NodeId) ->
  mnesia:activity(async_dirty,
  fun() ->
    mnesia:delete({node, NodeId})
  end
).

updateNode(Id, {NewX, NewY}) when is_integer(NewX) andalso is_integer(NewY) ->
  mnesia:activity(async_dirty,
    fun() ->
      [N] = mnesia:read(node, Id),
      mnesia:write(N#node{posX = NewX, posY = NewY})
    end
  );
% @ToDo: don't know what kind of guard should be used here...
updateNode(Id, Text) ->%when is_bitstring(Text) ->
  mnesia:activity(async_dirty,
    fun() ->
      [N] = mnesia:read(node, Id),
      mnesia:write(N#node{text = Text})
    end
  ).

getAll(TableName) when is_atom(TableName) ->
  mnesia:activity(async_dirty,
    fun() ->
      mnesia:select(TableName, ets:fun2ms(fun(N) -> N end))
    end
  ).

getAllNodes() ->
  getAll(node).

getNodeById(Id) when is_integer(Id) ->
  mnesia:activity(async_dirty,
    fun() ->
      [N] = mnesia:read(node, Id),
      N
    end
  ).


%@ToDo: make it general purpose (key as parameter)
updateFFID(NewValue) when is_integer(NewValue) ->
  mnesia:activity(async_dirty,
    fun() ->
      mnesia:write(#config{key = firstFreeId, val = NewValue})
    end
  ).

getOrCreateConfig(Key, NewValue) when is_integer(NewValue) ->
  mnesia:activity(async_dirty,
    fun() ->
      case mnesia:read({config, Key}) of
        [] ->
          mnesia:write(#config{key = Key, val = NewValue}),
          NewValue;
        [#config{val = Val}] -> Val;
        _ ->
          error_msg("Problem with config elemnt ~p. (multiple present?)", [Key])
      end
    end
  ).
