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
  io:format("PID: ~w \n", [self()]),
  process_flag(trap_exit, true),
  case gen_tcp:accept(ListenSocket) of
    {ok, AS} ->
      inet:setopts(AS, [{nodelay, true}, {packet, 4}]),
      goodGod:inf_clientConn(),
      goodGod:req_content(),
      loop(AS);
    {error, _} -> exit(socketFail)
  end.

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
    {gG, Type, Mesg} ->
      info_msg("Received request from gG"),
      dispatchSrvMessage(AS, Type, Mesg),
      loop(AS);
    {'EXIT', _, _} ->
      terminate(AS);
    A ->
      info_msg("Child received an unexpected present: ~p", [A]),
      loop(AS)
  end.

terminate(AcceptSocket) ->
  info_msg("Child is being terminated"),
  io:write(exitting),
  gen_tcp:close(AcceptSocket).

send_to_client(Socket, Msg) ->
  gen_tcp:send(Socket, Msg).

% called each time non-special message arrives over TCP
dispatchTcpMessage(_, Rec) when is_record(Rec, nodeCreated) ->
  goodGod:inf_nodeCreated(Rec);
dispatchTcpMessage(_, Rec) when is_record(Rec, idPoolContent) ->
  goodGod:req_id_range();
dispatchTcpMessage(Soc, test) ->
  send_to_client(Soc, "{\"mtype\":\"Test\",\"content\":{}}").

dispatchSrvMessage(Socket, retrans, Msg) ->
  case Msg of
    [] -> ok;
    M ->
      Em = messageEnDeCoder:encode(M),
      send_to_client(Socket, Em),
      ok
  end;
dispatchSrvMessage(Socket, content, Cont) ->
  case Cont of
    [] -> ok;
    C ->
      ContentCreatedList = lists:foldl(
        fun(X, Acc) ->
          [#nodeCreated{id = X#node.id, x = X#node.posX, y = X#node.posY}|Acc]
        end,
        [], C),
      Msg = messageEnDeCoder:encode(ContentCreatedList),
      send_to_client(Socket, Msg)
  end;
dispatchSrvMessage(Socket, idPool, {First,Last}) ->
  Em = messageEnDeCoder:encode(#idPoolContent{first = First, last = Last}),
  send_to_client(Socket, Em).

