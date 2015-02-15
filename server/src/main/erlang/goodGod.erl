%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 01. Dec 2014 9:28 PM
%%%-------------------------------------------------------------------
-module(goodGod).
-behaviour(gen_server).
-author("blueeyedhush").
-include("../include/global.hrl").
%% API
-export([spawn/0, init/1, handle_call/3, handle_cast/2, terminate/2, handle_info/2, val_cast/1,
  inf_clientConn/0, inf_nodeCreated/1, inf_clientDisconn/0, req_content/0, req_id_range/0, inf_nodeMoved/1, inf_nodeDeleted/1, inf_textSend/1]).

spawn() ->
  gen_server:start_link({local, goodGod}, ?MODULE, [], []).

init(_Args) ->
  info_msg("[gG] Started"),
  process_flag(trap_exit, true),

  database:start(),

  {ok, Port} = application:get_env(port),
  {ok, LS} = gen_tcp:listen(Port, [{active, true}, list]),
  io:format("~w \n", [LS]),

  {ok, DefIdPoolSize }= application:get_env(idPoolSize),
  {ok, DefFFID} = application:get_env(firstFreeId),
  FreeId = database:getOrCreateConfig(firstFreeId, DefFFID),
  IdPoolSize = database:getOrCreateConfig(idPoolSize, DefIdPoolSize),
  io:format("\n FFId: ~p \n", [FreeId]),
  State = #state{listenSocket = LS, clientList = [], nodeList = database:getAllNodes(), firstFreeId = FreeId, idPoolSize = IdPoolSize},

  %spawn first child
  guardianAngelSuperv:addChild(LS),
  {ok, State}.

handle_call(_Message, _From, State) ->
  {noreply, State}.

handle_cast(Message, State) ->
  case Message of
    {valcast, Mesg, From} ->
      case handle_val_cast(Mesg, From, State) of
        {reply,Reply,NewState} ->
          gen_server:cast(From, {gG, Mesg, Reply}),
          {noreply, NewState};
        {reply,Reply,NewState,Timeout} ->
          gen_server:cast(From, {gG, Mesg, Reply}),
          {noreply, NewState, Timeout};
        {reply,Reply,NewState,hibernate} ->
          gen_server:cast(From, {gG, Mesg, Reply}),
          {noreply, NewState, hibernate};
        Other ->
          Other
      end;
    _ -> handle_ord_cast(Message, State)
  end.

val_cast(Message) ->
  gen_server:cast(goodGod, {valcast, Message, self()}).

handle_ord_cast(_Message, State) -> {noreply, State}.

% can return same tuples as handle_call
handle_val_cast(Message, PID, State) ->
  case Message of

    clientConnected ->
      info_msg("[gG] Client connected"),
      guardianAngelSuperv:addChild(State#state.listenSocket),
      NewState = State#state{clientList = [PID|State#state.clientList]},
      {noreply, NewState};

    clientDisconnected ->
      info_msg("[gG] Client disconnected"),
      NewState = State#state{clientList = remove_from_list(PID, State#state.clientList, [])},
      {noreply, NewState};

    {nodeCreated, Descriptor} ->
      info_msg("[gG] New node created with coords: " ++ integer_to_list(Descriptor#nodeCreated.x) ++ " " ++ integer_to_list(Descriptor#nodeCreated.y)),
      Node = #node{id = Descriptor#nodeCreated.id, posX = Descriptor#nodeCreated.x, posY = Descriptor#nodeCreated.y, text = <<"">>},
      NewState = State#state{nodeList = [Node|State#state.nodeList]},
      broadcast_to_all_but(Descriptor, PID, NewState#state.clientList),
      database:createNode(Node),
      {noreply, NewState};

    {nodeMoved, Descriptor} ->
      info_msg("[gG] Node wants to be moved"),
      Res = updateNodeWithId(Descriptor#nodeMoved.id, State,
        fun(N) ->
          ModNode = N#node{posX = Descriptor#nodeMoved.x, posY = Descriptor#nodeMoved.y},
          database:updateNode(ModNode#node.id, {ModNode#node.posX, ModNode#node.posY}),
          ModNode
        end
      ),

      case Res of
        nodeNotPresent ->
          {noreply, State};
        NewState ->
          broadcast_to_all_but(Descriptor, PID, NewState#state.clientList),
          {noreply, NewState}
      end;

    {nodeDel, Descriptor} ->
      info_msg("[gG] Node wants to be deleted"),
      Res = updateNodeWithId(Descriptor#nodeDeleted.id, State,
        fun(N) ->
          database:deleteNode(Descriptor#nodeDeleted.id),
          discard
        end
      ),

      case Res of
        nodeNotPresent ->
          {noreply, State};
        NewState ->
          broadcast_to_all_but(Descriptor, PID, NewState#state.clientList),
          {noreply, NewState}
      end;

    {textSend, Descriptor} ->
      info_msg("[gG] Node text is being changed"),
      Res = updateNodeWithId(Descriptor#textSending.id, State,
        fun(N) ->
          ModNode = N#node{text = Descriptor#textSending.text},
          database:updateNode(ModNode#node.id, ModNode#node.text),
          ModNode
        end
      ),

      case Res of
        nodeNotPresent ->
          {noreply, State};
        NewState ->
          broadcast_to_all_but(Descriptor, PID, NewState#state.clientList),
          {noreply, NewState}
      end;

    reqContent ->
      {reply, database:getAllNodes(), State};

    reqIdRange ->
      if
        State#state.firstFreeId > 2147483647 - State#state.idPoolSize ->
          {stop, idRangeExhausted, State};
        true ->
          io:format("\nAssigned pool, firstId: ~p\n", [State#state.firstFreeId]),
          Last = State#state.firstFreeId + State#state.idPoolSize - 1,
          Mesg = {State#state.firstFreeId, Last},
          NewState = State#state{firstFreeId = Last + 1},
          database:updateFFID(Last + 1),
          {reply, Mesg, NewState}
      end

  end.

handle_info(Message, State) ->
  info_msg("[gG] Unexpected message has arrived: ~p", [Message]),
  {noreply, State}.

terminate(_Reason, State) ->
  info_msg("[gG] Terminating..."),
  gen_tcp:close(State#state.listenSocket),
  database:shutdown().

broadcast_to_all_but(_, _, []) -> ok;
broadcast_to_all_but(Record, But, [R|Recipients]) when R == But ->
  broadcast_to_all_but(Record, But, Recipients);
broadcast_to_all_but(Record, But, [R|Recipients]) when R /= But ->
  send_retransmit(R, Record),
  broadcast_to_all_but(Record, But, Recipients).

remove_from_list(El, [], Acc) ->
  lists:reverse(Acc);
remove_from_list(El, [A|List], Acc) when El == A ->
  remove_from_list(El, List, Acc);
remove_from_list(El, [A|List], Acc) ->
  remove_from_list(El, List, [A|Acc]).

% Updater can return 'discard' to remove node
updateNodeWithId(NodeId, State, Updater) ->
  case extractNodeById(NodeId, State) of
    {notFound, _} ->
      warning_msg(<<"Tried to update node which is not present">>),
      nodeNotPresent;
    {N, ListWithoutEl} when is_record(N, node) ->
      case Updater(N) of
        discard -> State#state{nodeList = ListWithoutEl};
        ModNode -> State#state{nodeList = [ModNode|ListWithoutEl]}
      end
  end.

extractNodeById(NodeId, State) ->
  find_single_matching_record(
    fun
      (P) when P#node.id == NodeId -> true;
      (_) -> false
    end, State#state.nodeList, []).

%also removed that element from the list, returns {Element, Rest}, changes order of element in the list
find_single_matching_record(_, [], Rest) ->
  {notFound, Rest};
find_single_matching_record(Pred, [H|T], AlreadyChecked) ->
  Res = Pred(H),
  if
    Res == true ->
      {H, AlreadyChecked ++ T};
    Res == false ->
      find_single_matching_record(Pred, T, [H|AlreadyChecked])
  end.

% wrappers for message-based communication with goodGod
inf_clientConn() ->
  val_cast(clientConnected).

inf_clientDisconn() ->
  val_cast(clientDisconnected).

inf_nodeCreated(Descriptor) ->
  val_cast({nodeCreated, Descriptor}).

inf_nodeMoved(Descriptor) ->
  val_cast({nodeMoved, Descriptor}).

inf_nodeDeleted(Desc) ->
  val_cast({nodeDel, Desc}).

inf_textSend(Desc) ->
  val_cast({textSend, Desc}).

req_content() ->
  val_cast(reqContent).

req_id_range() ->
  val_cast(reqIdRange).

send_retransmit(PID, Mesg) ->
  gen_server:cast(PID, {gG, retrans, Mesg}).