-module(callbackModule).
-behaviour(application).
-export([start/2, stop/1]).

start(_Type, _Args) ->
  supervisor:start_link(superv, []).

stop(_State) ->
	ok.
	
