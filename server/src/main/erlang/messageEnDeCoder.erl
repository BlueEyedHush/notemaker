%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 06. Dec 2014 9:50 PM
%%%-------------------------------------------------------------------
-module(messageEnDeCoder).
-author("blueeyedhush").

-include("../include/global.hrl").
-include("../include/jsonerl.hrl").

%% API
-export([
  encode/1,
  decode/1
]).

encode(_) -> notImpl.
%encode(Record) when is_record(Record, nodeCreated) ->
%  ["001 " | ?record_to_json(nodeCreated, Record)].

decode(Mesg) ->
  {MsgId, Json} = package_split(Mesg),
  case MsgId of
    1 -> ?json_to_record(nodeCreated, Json)
  end.

skip_non_digits([D|R]) when D >= 48, D =< 57 ->
  [D|R];
skip_non_digits([_|R]) ->
  skip_non_digits(R).

package_split(Msg) ->
  [S, D, J|Json] = skip_non_digits(Msg),
  info_msg(S), info_msg(D), info_msg(J),
  MsgId = (S - 48)*100 + (D - 48)*10 + (J - 48),
  {MsgId, Json}.
