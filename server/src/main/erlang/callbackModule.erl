-module(callbackModule).
-behaviour(application).
-export([start/2, stop/1]).

start(normal, _Args) ->
	supervisor:start_link(consolidator, []).

stop(_State) ->
	ok.
	
