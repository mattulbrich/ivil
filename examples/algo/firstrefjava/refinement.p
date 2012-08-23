# Automatically created on Wed Aug 22 18:14:17 CEST 2012
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
 sourceline 28
  goto body0, after0
 body0:
  assume (!(sA) = emptyset); "assume condition "
 sourceline 32
  skip MARK, 1, ((sA = seqAsSet(seqSub(arrayAsIntSeq(h, _array), _i, arrlen(_array)))) & ((_i >= 0) & ((_i < arrlen(_array)) & (sumA = _result)))), 42 ; "marking stone"
 sourceline 33
  havoc xA
  assume (xA :: sA)
 sourceline 34
  sumA := (sumA + xA)
 sourceline 35
  sA := (sA \ singleton(xA))
 sourceline 36
  skip MARK, 2, ((sA = seqAsSet(seqSub(arrayAsIntSeq(h, _array), _i, arrlen(_array)))) & ((_i >= 0) & (sumA = _result))), 42 ; "marking stone"
  goto loop0
 sourceline 28
 after0:
  assume $not((!(sA) = emptyset))
 endOfProgram: 


problem ((setA = seqAsSet(arrayAsIntSeq(h, _array))) & ((!(_array) = null) & (\forall a; (((0 <= a) & (a < arrlen(_array))) -> (\forall b; (((0 <= b) & (b < arrlen(_array))) -> ((h[_array, idxInt(a)] = h[_array, idxInt(b)]) -> (a = b)))))))) |- [0; Java][<0;Sum>]((sumA = resInt))
