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

encode(Record) when is_record(Record, nodeCreated) ->
  Content = ?record_to_json(nodeCreated, Record),
  "{\"mtype\":\"NodeCreated\",\"content\":" ++ Content ++ "}".

decode(Mesg) ->
  Proplist = parse(Mesg),
  MesgType = remove_all_occurences($", proplists:get_value("mtype", Proplist)),
  JsonContent = proplists:get_value("content", Proplist),
  Record =
    case MesgType of
      "NodeCreated" -> ?json_to_record(nodeCreated, JsonContent)
    end.

parse(Mesg) ->
  top_level_rec(Mesg, whatever, whatever, []).
top_level_rec("{" ++ Rest, Key, value, TupleList) ->
  {S, R} = extract(${, $}, Rest),
  top_level_rec(R, whatever, whatever, [{Key, "{" ++ S}|TupleList]);
top_level_rec("[" ++ Rest, Key, value, TupleList) ->
  {S, R} = extract($[, $], Rest),
  top_level_rec(R, whatever, whatever, [{Key, "[" ++ S}|TupleList]);
top_level_rec("\"" ++ Rest, Key, value, TupleList) ->
  {S, R} = extract($", $", Rest),
  top_level_rec(R, whatever, whatever, [{Key, "\""++ S}|TupleList]);
top_level_rec("\"" ++ Rest, Key, Mode, TupleList) ->
  {S, R} = extract($", $", Rest),
  [_|KeyRev] = lists:reverse(S), FinKey = lists:reverse(KeyRev),
  {_, R1} = skip_up_to($:, R),
  [_|R2] = R1,
  top_level_rec(R, FinKey, value, TupleList);
top_level_rec("}" ++ Rest, Key, Mode, TupleList) ->
  TupleList;
top_level_rec("{" ++ Rest, Key, Mode, TupleList) ->
  top_level_rec(Rest, Key, Mode, TupleList);
top_level_rec([_|Rest], Key, Mode, TupleList) ->
  top_level_rec(Rest,Key, Mode, TupleList).


% jeśli wejscie nie bedzie poprawne (czyli liczba otwierajacych o jeden mniejsza niz zamykajacych)
% spektakularnie sie przewroci
extract(Op, Cl, Str) ->
  skip_rec(Op, Cl, 1, Str, []).
skip_rec(_, _, 0, Rest, Skipped) ->
  {lists:reverse(Skipped), Rest};
% istotne jest by jeśli Opening = Closing dopasował do Closing, dlatego pierwsze
skip_rec(Opening, Closing, Depth, [Next|Rest], Skipped) when Closing == Next ->
  skip_rec(Opening, Closing, Depth-1, Rest, [Next|Skipped]);
skip_rec(Opening, Closing, Depth, [Next|Rest], Skipped) when Opening == Next ->
  skip_rec(Opening, Closing, Depth+1, Rest, [Next|Skipped]);
skip_rec(Opening, Closing, Depth, [A|Rest], Skipped) ->
  skip_rec(Opening, Closing, Depth, Rest, [A|Skipped]).

skip_up_to(Char, List) ->
    skip_up_to_rec(Char, [], List).
skip_up_to_rec(Char, Skipped, [A|R]) when A /= Char ->
  skip_up_to_rec(Char, [A|Skipped], R);
skip_up_to_rec(Char, Skipped, [A|R]) when A == Char ->
  {lists:reverse(Skipped), [A|R]}.

remove_all_occurences(What, Src) ->
  remove_all_occurences_rec(What, Src, []).
remove_all_occurences_rec(What, [], Dest) ->
  lists:reverse(Dest);
remove_all_occurences_rec(What, [A|Src], Dest) when A == What ->
  remove_all_occurences_rec(What, Src, Dest);
remove_all_occurences_rec(What, [A|Src], Dest) ->
  remove_all_occurences_rec(What, Src, [A|Dest]).