# Created by RuleJustification
# Wed Jul 04 23:08:20 CEST 2012

include
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"
  "$seq.p"


function seq(int) seqId(int)
function seq('a) seqSwap(seq('a), int, int)
function seq(int) seqInv(seq(int))
function bool seqDom(seq('a), int)
function bool isPerm(seq('a), seq('a))
function bool isPermN(seq(int))
function bool isSorted(seq(int))
function int count(seq('a), 'a)


axiom isSorted_def
  (\forall a as seq(int);$eq(isSorted(\var a as seq(int)) as bool,(\forall i as int;$impl(seqDom(\var a as seq(int),\var i as int) as bool,(\forall j as int;$impl($and(seqDom(\var a as seq(int),\var j as int) as bool,$lt(\var i as int,\var j as int) as bool) as bool,$lte(seqGet(\var a as seq(int),\var i as int) as int,seqGet(\var a as seq(int),\var j as int) as int) as bool) as bool) as bool) as bool) as bool) as bool) as bool

properties
  SimplificationStrategy.splitMode "DONT_SPLIT"

rule seqSwapDef
  find seqSwap(%a as seq(%'v10),%i as int,%j as int) as seq(%'v10)
  where freshVar %t, %a, %i, %j
  samegoal
    replace (\seqDef %t as int;0 as int;seqLen(%a as seq(%'v10)) as int;cond($eq(%t as int,%i as int) as bool,seqGet(%a as seq(%'v10),%j as int) as %'v10,cond($eq(%t as int,%j as int) as bool,seqGet(%a as seq(%'v10),%i as int) as %'v10,seqGet(%a as seq(%'v10),%t as int) as %'v10) as %'v10) as %'v10) as seq(%'v10)

rule seqIdDef
  find seqId(%n as int) as seq(int)
  where freshVar %t, %n
  samegoal
    replace (\seqDef %t as int;0 as int;%n as int;%t as int) as seq(int)

rule inDom_def
  find seqDom(%a as seq(%'v1),%i as int) as bool
  samegoal
    replace $and($lte(0 as int,%i as int) as bool,$lt(%i as int,seqLen(%a as seq(%'v1)) as int) as bool) as bool
  tags
    asAxiom ""

rule isPermN_def
  find isPermN(%p as seq(int)) as bool
  samegoal
    replace (\forall i as int;$impl(seqDom(%p as seq(int),\var i as int) as bool,(\exists j as int;$and(seqDom(%p as seq(int),\var j as int) as bool,$eq(seqGet(%p as seq(int),\var j as int) as int,\var i as int) as bool) as bool) as bool) as bool) as bool

rule isPerm_def
  find isPerm(%a as seq(%'v14),%b as seq(%'v14)) as bool
  samegoal
    replace $and($eq(seqLen(%a as seq(%'v14)) as int,seqLen(%b as seq(%'v14)) as int) as bool,(\exists p as seq(int);$and($and(isPermN(\var p as seq(int)) as bool,$eq(seqLen(\var p as seq(int)) as int,seqLen(%a as seq(%'v14)) as int) as bool) as bool,(\forall i as int;$impl(seqDom(\var p as seq(int),\var i as int) as bool,$eq(seqGet(%a as seq(%'v14),\var i as int) as %'v14,seqGet(%b as seq(%'v14),seqGet(\var p as seq(int),\var i as int) as int) as %'v14) as bool) as bool) as bool) as bool) as bool) as bool

rule seqInvDef
  find seqInv(%s as seq(int)) as seq(int)
  where freshVar %i, %s
  where freshVar %j, %s
  samegoal
    replace (\seqDef %i as int;0 as int;seqLen(%s as seq(int)) as int;(\some %j as int;$and(seqDom(%s as seq(int),%j as int) as bool,$eq(seqGet(%s as seq(int),%j as int) as int,%i as int) as bool) as bool) as int) as seq(int)

rule lenSeqId
  find seqLen(seqId(%n as int) as seq(int)) as int
  samegoal
    replace cond($gte(%n as int,0 as int) as bool,%n as int,0 as int) as int
  tags
    rewrite "fol simp"
    asAxiom ""
    derived ""

rule getOfId
  find seqGet(seqId(%n as int) as seq(int),%i as int) as int
  samegoal
    replace cond($and($lte(0 as int,%i as int) as bool,$lt(%i as int,%n as int) as bool) as bool,%i as int,seqError as int) as int
  tags
    asAxiom ""
    derived ""

rule isSorted_def
  find isSorted(%a as seq(int)) as bool
  where freshVar %i, %a
  where freshVar %j, %a
  samegoal
    replace (\forall %i as int;$impl(seqDom(%a as seq(int),%i as int) as bool,(\forall %j as int;$impl($and(seqDom(%a as seq(int),%j as int) as bool,$lt(%i as int,%j as int) as bool) as bool,$lte(seqGet(%a as seq(int),%i as int) as int,seqGet(%a as seq(int),%j as int) as int) as bool) as bool) as bool) as bool) as bool


function int k
function int j
function int i
function seq('v12) a


problem
  $eq(seqGet(seqSwap(a as seq('v12),i as int,j as int) as seq('v12),k as int) as 'v12,cond($and($lte(0 as int,k as int) as bool,$lt(k as int,seqLen(a as seq('v12)) as int) as bool) as bool,cond($eq(i as int,k as int) as bool,seqGet(a as seq('v12),j as int) as 'v12,cond($eq(j as int,k as int) as bool,seqGet(a as seq('v12),i as int) as 'v12,seqGet(a as seq('v12),k as int) as 'v12) as 'v12) as 'v12,seqError as 'v12) as 'v12) as bool
