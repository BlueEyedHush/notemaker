{application, ${ARTIFACT},
  [{description, ${DESCRIPTION}},
   {id, ${ID}},
   {vsn, ${VERSION}},
   {modules, ${MODULES}},
   {maxT, infinity},
   {registered, ${REGISTERED}},
   {included_applications, []},
   {applications, [kernel, stdlib, sasl]},
   {env, [
     {port, 36500},
     {idPoolSize, 10},
     {firstFreeId, -2147483648},
     {dbLoc, <<"/home/ubuntu/mnesia">>}
   ]},
   {start_phases, []},
   {mod, {callbackModule, []}}
]}.
