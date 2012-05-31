# Created by RuleJustification
# Thu May 31 18:38:17 CEST 2012

include
  "$set.p"
  "$pair.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

sort vertex

function set(vertex) succ(vertex)
function bool connect(vertex, vertex, int)
function bool minconnect(vertex, vertex, int)

axiom connect_def
  (\forall a;(\forall b;(\forall n;$eq(connect(\var a,\var b,\var n),cond($lte(\var n,0),$eq(\var a,\var b),(\exists x;$and(connect(\var a,\var x,$minus(\var n,1)),$mem(\var b,succ(\var x)))))))))

axiom minconnect_def
  (\forall a;(\forall b;(\forall n;$and($eq(minconnect(\var a,\var b,\var n),connect(\var a,\var b,\var n)),(\forall m;$impl($and($lte(0,\var m),$lt(\var m,\var n)),$not(connect(\var a,\var b,\var m))))))))

properties
  CompoundStrategy.strategies "HintStrategy,SimplificationStrategy,BreakpointStrategy,SMTStrategy"

rule connect_def
  find connect(%a,%b,%n)
  where freshVar %x, %a, %b, %n
  samegoal
    replace cond($lte(%n,0),$eq(%a,%b),(\exists %x;$and(connect(%a,%x,$minus(%n,1)),$mem(%b,succ(%x)))))
  tags
    derived ""


function vertex a
function vertex b
function int n

problem
  $eq(minconnect(a,b,n),$and(connect(a,b,n),(\forall m;$impl($and($lte(0,\var m),$lt(\var m,n)),$not(connect(a,b,\var m))))))
