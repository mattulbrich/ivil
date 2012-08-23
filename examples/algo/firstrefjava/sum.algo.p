# Automatically created on Wed Aug 22 18:14:18 CEST 2012
include "sum.decl.p"
include "Sum.sum(int[]).p"
function set(int) setA 
function int sumA assignable
function set(int) sA assignable
function int xA assignable

program Sum source "sum.algo"
 sourceline 26
  sumA := 0
 sourceline 27
  sA := setA
 loop0:
 sourceline 29
  skip LOOPINV, true, sA
 sourceline 28
  goto body0, after0
 body0:
  assume (!(sA) = emptyset); "assume condition "
 sourceline 33
  assert (\exists xA; (xA :: sA)) ; "assert before choose"
  havoc xA
  assume (xA :: sA)
 sourceline 34
  sumA := (sumA + xA)
 sourceline 35
  sA := (sA \ singleton(xA))
  goto loop0
 sourceline 28
 after0:
  assume $not((!(sA) = emptyset))
 endOfProgram: 

