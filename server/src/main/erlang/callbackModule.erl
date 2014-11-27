-module(callbackModule).
-behaviour(application).
-export([start/2, stop/1]).

start(normal, _Args) ->
	io:write(appTest).

stop(_State) ->
	ok.
	
