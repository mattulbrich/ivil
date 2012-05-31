include "$set.p"
include "$pair.p"
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



axiom connect_def
  (\forall a; (\forall b; (\forall n;
    connect(a,b,n) = 
     cond(n <= 0,
          a = b,
          (\exists x; connect(a,x,n-1) & b::succ(x))))))

axiom minconnect_def
  (\forall a; (\forall b; (\forall n;
    minconnect(a,b,n) = connect(a,b,n) &
                        (\forall m; 0<=m & m <n -> !connect(a,b,m)))))

(*
 * The following rules apply the definitions from above
 * and are, hence, derived
 *)

rule connect_def
  find connect(%a, %b, %n)
  where freshVar %x, %a, %b, %n
  replace cond(%n <= 0,
        %a = %b,
        (\exists %x; connect(%a,%x,%n-1) & %b::succ(%x)))
  tags derived

rule minconnect_def
  find minconnect(%a, %b, %n)
  where freshVar %m, %a, %b, %n
  replace connect(%a, %b, %n) &
        (\forall %m; 0 <= %m & %m < %n -> !connect(%a, %b, %m))
  tags derived

rule oops
closegoal

rule nested_quant_z3
  where askDecisionProcedure
  closegoal
  tags
    decisionProcedure "Z3"
    timeout "4000"
    additionalParams "PULL_NESTED_QUANTIFIERS=true"
    autoonly