include "$set.p"
include "$pair.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"

properties
  CompoundStrategy.strategies 
    "HintStrategy,SimplificationStrategy,BreakpointStrategy"
    #,SMTStrategy

sort vertex

function set(vertex) succ(vertex)

function 
  bool connect(vertex, vertex, int)
  bool minconnect(vertex, vertex, int)

rule connect_def
  find connect(%a, %b, %n)
  where freshVar %x, %a, %b, %n
  replace cond(%n <= 0,
        %a = %b,
        (\exists %x; connect(%a,%x,%n-1) & %b::succ(%x)))

axiom connect_def
  (\forall a; (\forall b; (\forall n;
    connect(a,b,n) = 
     cond(n <= 0,
          a = b,
          (\exists x; connect(a,x,n-1) & b::succ(x))))))

rule minconnect_def
  find minconnect(%a, %b, %n)
  where freshVar %m, %a, %b, %n
  replace connect(%a, %b, %n) &
        (\forall %m; 0 <= %m & %m < %n -> !connect(%a, %b, %m))

axiom minconnect_def
  (\forall a; (\forall b; (\forall n;
    minconnect(a,b,n) = connect(a,b,n) &
                        (\forall m; 0<=m & m <n -> !connect(a,b,m)))))

rule minconnect_plus1
  find minconnect(%a, %b, %n) |-
  where freshVar %q, %a, %b, %n
  add %n > 0 -> (\exists %q; minconnect(%a, %q, %n-1) & %b :: succ(%q)) |-
  tags derived

(* this rule is not confluent *)
rule minconnect_rec
  find minconnect(%a, %b, %n)
  where freshVar %q, %a, %b, %n
  where freshVar %m, %a, %b, %n
  replace cond(%n <= 0,
        %a = %b,
        (\exists %q; minconnect(%a,%q,%n-1) & %b::succ(%q))
        & (\forall %q; (\forall %m; %b::succ(%q) & 0<=%m & %m<%n-1 -> 
           !minconnect(%a, %q, %m))))
  tags derived
  

rule oops
closegoal

rule nested_quant_z3
  where askDecisionProcedure
  closegoal
  tags
    decisionProcedure "Z3"
    timeout "2000"
    additionalParams "PULL_NESTED_QUANTIFIERS=true"
    autoonly