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
-include("../include/global.hrl").

%% API
-export([
  start/1
]).

start(ListenSocket) ->
  info_msg("New guardianAngel started"),
  {ok, AS} = gen_tcp:accept(ListenSocket),
  goodGod:inf_clientConn(),
  loop(AS).

loop(AS) ->
  receive
    {tcp, Socket, "{testquery}"} ->
      gen_tcp:send(Socket, "{testresponse}\n"),
      info_msg("Sent {testresponse}"),
      loop(AS);
    {tcp, Socket, Msg} ->
      info_msg("Received TCP message: \n" ++ Msg),
      ConvertedMesg = messageEnDeCoder:decode(Msg),
      dispatchTcpMessage(Socket, ConvertedMesg),
      loop(AS);
    {tcp_closed, _} ->
      info_msg("Socket closed, so child is exiting"),
      goodGod:inf_clientDisconn(),
      exit(normal);
    {gG, Mesg} ->
      dispatchSrvMessage(AS, Mesg),
      loop(AS);
    A ->
      info_msg("Child received an unexpected present: ~p", [A]),
      loop(AS)
  end.


% called each time non-special message arrives over TCP
dispatchTcpMessage(_, Rec) when is_record(Rec, nodeCreated) ->
  goodGod:inf_nodeCreated(Rec).

dispatchSrvMessage(Socket, Msg) ->
  Em = messageEnDeCoder:encode(Msg),
  gen_tcp:send(Socket, Em).