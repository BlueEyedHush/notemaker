%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 27. Nov 2014 9:32 AM
%%%-------------------------------------------------------------------
-module(guardianAngel).
-behaviour(gen_server).
-author("blueeyedhush").
-include("../include/global.hrl").

%% API
-export([
  spawn/1, init/1, handle_call/3, handle_cast/2, handle_info/2, terminate/2
]).

spawn(ListenSocket) ->
  gen_server:start_link(?MODULE, {ListenSocket},[]).

init({ListenSocket}) ->
  info_msg("New guardianAngel started"),
  io:format("PID: ~w \n", [self()]),

  process_flag(trap_exit, true),

  gen_server:cast(self(), waitForClient),
  {ok, #guardian_state{listen_socket = ListenSocket}}.

handle_call(_Message, _From, State) ->
  {noreply, State}.

handle_cast(Message, State) ->
  case Message of
    waitForClient ->
      case gen_tcp:accept(State#guardian_state.listen_socket) of
        {ok, AS} ->
          inet:setopts(AS, [{nodelay, true}, {packet, 4}]),
          goodGod:inf_clientConn(),
          goodGod:req_content(),
          {noreply, State#guardian_state{accept_socket = AS}};
        {error, _} -> {stop, accept_failure, State}
      end;
   {gG, Type, Mesg} ->
     info_msg("Received request from gG"),
     dispatchSrvMessage(State#guardian_state.accept_socket, Type, Mesg),
     {noreply, State}
  end.

handle_info(Message, State) ->
  case Message of
    {tcp, Socket, Msg} ->
      info_msg("Received TCP message: \n" ++ Msg),
      ConvertedMesg = messageEnDeCoder:decode(Msg),
      dispatchTcpMessage(Socket, ConvertedMesg);
    {tcp_closed, _} ->
      info_msg("Socket closed, so child is exiting"),
      goodGod:inf_clientDisconn(),
      exit(normal);
    A ->
      info_msg("Child received an unexpected present: ~p", [A])
  end,
  {noreply, State}.

terminate(_Reason, State) ->
  info_msg("Child is being terminated"),
  gen_tcp:close(State#guardian_state.accept_socket).

send_to_client(Socket, Msg) ->
  gen_tcp:send(Socket, Msg).

% called each time non-special message arrives over TCP
dispatchTcpMessage(_, Rec) when is_record(Rec, nodeCreated) ->
  goodGod:inf_nodeCreated(Rec);
dispatchTcpMessage(_, Rec) when is_record(Rec, nodeMoved) ->
  goodGod:inf_nodeMoved(Rec);
dispatchTcpMessage(_, Rec) when is_record(Rec, nodeDeleted) ->
  goodGod:inf_nodeDeleted(Rec);
dispatchTcpMessage(_, Rec) when is_record(Rec, textSending) ->
  goodGod:inf_textSend(Rec);
dispatchTcpMessage(_, Rec) when is_record(Rec, idPoolContent) ->
  info_msg(<<"dispatchTcpMessage: idPool request">>),
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
dispatchSrvMessage(Socket, reqContent, Cont) ->
  case Cont of
    [] -> ok;
    C ->
      ContentCreatedList = lists:foldl(
        fun(X, Acc) ->
          [#nodeCreated{id = X#node.id, x = X#node.posX, y = X#node.posY, text = X#node.text}|Acc]
        end,
        [], C),
      Msg = messageEnDeCoder:encode(ContentCreatedList),
      send_to_client(Socket, Msg)
  end;
dispatchSrvMessage(Socket, reqIdRange, {First,Last}) ->
  Em = messageEnDeCoder:encode(#idPoolContent{first = First, last = Last}),
  send_to_client(Socket, Em).

