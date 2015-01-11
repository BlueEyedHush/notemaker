%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 27. Dec 2014 8:41 AM
%%%-------------------------------------------------------------------
-module(id_test).
-author("blueeyedhush").

-include("../../main/include/global.hrl").
-include_lib("eunit/include/eunit.hrl").

% @ToDo: Make it work!

%id_test_() ->
%  {setup,
%    fun setup/0,
%    fun cleanup/1,
%    fun testsuite/1
%  }.

setup() ->
  application:ensure_started(sasl),
  application:start(notemakerSrv),
  {ok, Socket} = gen_tcp:connect({127,0,0,1}, 36500, [list, {active,true},{nodelay, true}, {packet, 4}, {reuseaddr, true}]),
  {Socket}.

testsuite({Socket}) ->
  [?_test(isServerStarted(Socket)),
    ?_test(isIdRangeAssignedCorrectly(Socket))].

cleanup({Socket}) ->
  gen_tcp:close(Socket),
  application:stop(notemakerSrv).

wait() ->
  io:write(<<"WRITE \n">>),
  receive
    {tcp, _, Msg} ->
      ok;
    _ -> io:write(w), wait()
  %after
  %  3000 -> exit(timeout)
  end.

isServerStarted(Socket) ->
  gen_tcp:send(Socket, "{\"mtype\":\"Test\",\"content\":{}}"),
  wait().


isIdRangeAssignedCorrectly(Socket) ->
  gen_tcp:send(Socket, "{\"mtype\":\"IdPool\",\"content\":{\"type\":\"IdPoolContent\",\"first\":0,\"last\":0}}"),
  receive
    {tcp, _, Msg} ->
      notImpl
  after
    1000 ->
      exit(timeout)
  end.