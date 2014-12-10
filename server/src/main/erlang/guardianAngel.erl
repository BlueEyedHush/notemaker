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
  process_flag(trap_exit, true),
  {ok, AS} = gen_tcp:accept(ListenSocket),
  inet:setopts(AS, [{nodelay, true}, {packet, 4}]),
  goodGod:inf_clientConn(),
  loop(AS).

loop(AS) ->
  receive
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
      info_msg("Received retransmission request from gG"),
      dispatchSrvMessage(AS, Mesg),
      loop(AS);
    {'EXIT', _, _} ->
      terminate(AS);
    A ->
      info_msg("Child received an unexpected present: ~p", [A]),
      loop(AS)
  end.

terminate(AcceptSocket) ->
  info_msg("Child is begin terminated"),
  gen_tcp:close(AcceptSocket).

%WARNING!
%
%Messages sent to client must be appended with \n

send_to_client(Socket, Msg) ->
  gen_tcp:send(Socket, Msg ++ "\n").

% called each time non-special message arrives over TCP
dispatchTcpMessage(_, Rec) when is_record(Rec, nodeCreated) ->
  goodGod:inf_nodeCreated(Rec);
dispatchTcpMessage(Soc, test) ->
  send_to_client(Soc, "{\"mtype\":\"Test\", \"content\":{}}").

dispatchSrvMessage(Socket, Msg) ->
  Em = messageEnDeCoder:encode(Msg),
  send_to_client(Socket, Em).