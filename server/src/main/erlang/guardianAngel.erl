%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 27. Nov 2014 9:32 AM
%%%-------------------------------------------------------------------
-module(guardianAngel).
-author("blueeyedhush").

%% API
-export([
  start/1
]).

start(AcceptSocket) ->
  io:format("guardianAngel started"),
  loop(AcceptSocket).

loop(AC) ->
  receive
    {tcp, _Socket, "{testquery}"} ->
      gen_tcp:send(AC, "{testresponse}"),
      io:format("Sent {testresponse}"),
      loop(AC);
    _ ->
      io:format("sth arrived!"),
      loop(AC)
  end.
