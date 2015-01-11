%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2015, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 11. Jan 2015 1:22 AM
%%%-------------------------------------------------------------------
-module(utils_test).
-author("blueeyedhush").

-include("../../main/include/global.hrl").
-include_lib("eunit/include/eunit.hrl").

-record(testRec, {a, b}).

find_single_matching_record_test() ->
  TestList = [#testRec{a = "A", b = 1}, #testRec{a = "B", b = 2}],

  Res1 = goodGod:find_single_matching_record(fun (P) -> case P#testRec.a of "A" -> true; _ -> false end end, TestList, []),
  ?assertMatch({#testRec{a = "A", b = 1}, [#testRec{a = "B", b = 2}]}, Res1),

  %Res2 = goodGod:find_single_matching_record(#testRec{a = "A", b = 1}, TestList, []),
  %?assertMatch({#testRec{a = "A", b = 1}, [#testRec{a = "B", b = 2}]}, Res2),
  %
  Res3 = goodGod:find_single_matching_record(fun (P) -> case P#testRec.a of "C" -> true; _ -> false end end, TestList, []),
  ?assertMatch({notFound, [#testRec{a = "B", b = 2}, #testRec{a = "A", b = 1}]}, Res3).
