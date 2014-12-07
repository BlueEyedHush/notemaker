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

isJsonCorrectlyDecoded_test() ->
  String = "
    { \"mtype\" : \"NodeCreated\",
      \"content\" : {
        \"x\" : 120,
        \"y\" : 60
      }
    }
    ",
  GenericMsg = messageEnDeCoder:decode(String),
  Pattern = #nodeCreated{x = 120, y = 60},
  ?assertEqual(Pattern, GenericMsg).

%isJsonCorrectlyEncoded_test() ->
%  Record = #nodeCreated{x = 120, y = 60},
%  Json = messageEnDeCoder:encode(Record),
%  ?assertEqual("{\"mtype\":\"NodeCreated\",\"content\":{\"x\"=120,\"y\"=60}}", Json).

isExtractorWorkingWithCurlyBraces_test() ->
  TestString = "abcdef { akrlkj {sdfs}sdfs}sdf}rest",
  {Skipped, Rest} = messageEnDeCoder:extract(${, $}, TestString),
  ?assertEqual("rest", Rest),
  ?assertEqual("abcdef { akrlkj {sdfs}sdfs}sdf}", Skipped).

isExtractorWorkingWithDQuotes_test() ->
  TestString1 = "Ala\" ma kota",
  {Skipped1, Rest1} = messageEnDeCoder:extract($", $", TestString1),
  ?assertEqual(" ma kota", Rest1),
  ?assertEqual("Ala\"", Skipped1).

isExtractorWorkingWithSquareBraces_test() ->
  TestString = "first [second  [third [fourth ] third] second] first] rest",
  {Skipped, Rest} = messageEnDeCoder:extract($[, $], TestString),
  ?assertEqual(" rest", Rest),
  ?assertEqual("first [second  [third [fourth ] third] second] first]", Skipped).

isParsingWorking_test() ->
  TestJson = "{ \"mtype\" : \"NodeCreated\", \"content\" : {content}}",
  Proplist = messageEnDeCoder:parse(TestJson),
  ?assertEqual("\"NodeCreated\"", proplists:get_value("mtype", Proplist)),
  ?assertEqual("{content}", proplists:get_value("content", Proplist)).


