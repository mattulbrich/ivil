include "$heap.p"
include "$seq.p"
include "$set.p"
include "$pair.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"

properties
  CompoundStrategy.strategies 
    "HintStrategy,SimplificationStrategy,BreakpointStrategy,SMTStrategy"

plugin 
  contextExtension :
    "de.uka.iti.pseudo.gui.extensions.PropositionalExpansionExt"

sort vertex

function set(vertex) succ(vertex)
function seq(bool) a_succ(int)

function 
  bool connect(vertex, vertex, int)
  bool minconnect(vertex, vertex, int)



axiom connect_def
  (\forall a; (\forall b; (\forall n;
    connect(a,b,n) = 
     cond(n <= 0,
          a = b,
          (\exists x; connect(a,x,n-1) & b :: succ(x))))))

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


(*
 * Rule for array as sequences
 *)

function
  seq(bool) boolArrAsSeq(heap, ref)

rule boolArrAsSeqDef
  find boolArrAsSeq(%h, %r)
  where freshVar %x, %h, %r
  replace (\seqDef %x; 0; arrlen(%r); %h[%r, idxBool(%x)])

rule getOfBoolArrSeq
  find seqGet(boolArrAsSeq(%h, %r), %i)
  replace cond(0 <= %i & %i < arrlen(%r), %h[%r, idxBool(%i)], seqError)
  tags derived
       rewrite "fol simp"
       asAxiom

rule lenOfBoolArrSeq
  find seqLen(boolArrAsSeq(%h, %r))
  replace arrlen(%r)
  tags derived
       rewrite "fol simp"
       asAxiom