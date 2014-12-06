%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 06. Dec 2014 8:00 PM
%%%-------------------------------------------------------------------
-module(messageEnDeCoder_test).
-author("blueeyedhush").

-include("../../main/include/global.hrl").
-include_lib("eunit/include/eunit.hrl").

isJsonCorrectlyDecodedEncoded_test() ->
  String = "
    001 {
      \"x\" : 120,
      \"y\" : 60
    }
    ",
  GenericMsg = messageEnDeCoder:decode(String),
  Pattern = #nodeCreated{x = 120, y = 60},
  ?assertEqual(Pattern, GenericMsg).
  %Json = messageEnDeCoder:encode(Pattern),
  %?assertEqual(String, Json).
