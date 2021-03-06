%%%-------------------------------------------------------------------
%%% @author blueeyedhush
%%% @copyright (C) 2014, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 03. Dec 2014 12:30 PM
%%%-------------------------------------------------------------------
-author("blueeyedhush").

-import(error_logger, [
  error_msg/1,
  error_msg/2,
  warning_msg/1,
  warning_msg/2,
  info_msg/1,
  info_msg/2
]).

% Do rekordów zawsze muszą być wpisywane binary stringi
% Nowo utworzone rekordy będą miały stringi w postci binary string
% Z kolei funkcje dekodujące będą wyrzucać listowe stringi
-record(state, {listenSocket, clientList, nodeList, firstFreeId, idPoolSize}).
-record(guardian_state, {accept_socket, listen_socket}).
-record(node, {id, posX, posY, text}).
-record(config, {key, val}).

-record(nodeCreated, {type = <<"NodeCreatedContent">>, id :: integer(), x :: integer(), y :: integer(), text}).
%@ToDo: Shouldn't this content contain 'type' member?
-record(containerContent, {mlist}).
-record(idPoolContent, {type = <<"IdPoolContent">>, first, last}).
-record(nodeMoved, {type = <<"NodeMovedContent">>, id, x, y}).
-record(nodeDeleted, {type = <<"NodeDeletedContent">>, id}).
-record(textSending, {type = <<"NodeMessageContent">>, id, text}).