-module(callbackModule).
-behaviour(application).
-include("../include/global.hrl").

-export([start/2, stop/1]).

start(_Type, _Args) ->
  supervisor:start_link(superv, []).

stop(_State) ->
	ok.
	
