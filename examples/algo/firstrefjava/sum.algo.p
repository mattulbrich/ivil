# Automatically created on Tue Jul 03 17:45:51 CEST 2012
include "$refinement.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"
include "$set.p"
include "$seq.p"
include "sum.decl.p"
include "java-out/Sum.sum([I)I-1.p"
function set(int) setA 
function int sumA assignable
function set(int) sA assignable
function int xA assignable

program Sum source "sum.algo"
 sourceline 31
  sumA := 0
 sourceline 32
  sA := setA
 loop0:
 sourceline 34
  skip LOOPINV, true, sA
 sourceline 33
  goto body0, after0
 body0:
  assume (!(sA) = emptyset); "assume condition "
 sourceline 38
  assert (\exists xA; (xA :: sA)) ; "assert before choose"
  havoc xA
  assume (xA :: sA)
 sourceline 39
  sumA := (sumA + xA)
 sourceline 40
  sA := (sA \ singleton(xA))
  goto loop0
 sourceline 33
 after0:
  assume $not((!(sA) = emptyset))
 endOfProgram: 

