include "$set.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"

properties
  CompoundStrategy.strategies 
    "HintStrategy,SimplificationStrategy,BreakpointStrategy,SMTStrategy"

sort vertex

function set(vertex) succ(vertex)

function 
  bool connect(vertex, vertex, int)
  bool minconnect(vertex, vertex, int)

rule connect_def
  find connect(%a, %b, %n)
  where freshVar %x, %a, %b, %n
  replace cond(%n=0,
        %a = %b,
        (\exists %x; connect(%a,%x,%n-1) & %b::succ(%x)))

rule minconnect_def
  find minconnect(%a, %b, %n)
  where freshVar %m, %a, %b, %n
  replace connect(%a, %b, %n) &
        (\forall %m; 0 <= %m & %m < %n -> !connect(%a, %b, %m))

rule oops
closegoal