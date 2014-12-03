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

start(ListenSocket) ->
  io:format("guardianAngel started\n"),
  {ok, AS} = gen_tcp:accept(ListenSocket),
  goodGod ! clientConnected,
  loop(AS).

loop(AS) ->
  receive
    {tcp, Socket, "{testquery}"} ->
      gen_tcp:send(Socket, "{testresponse}\n"),
      io:format("Sent {testresponse}\n"),
      loop(AS);
    {tcp_closed, _} ->
      io:format("Socket closed, so child is exiting\n"),
      exit(normal);
    A ->
      io:format("sth arrived!\n"),
      io:write(A),
      loop(AS)
  end.
