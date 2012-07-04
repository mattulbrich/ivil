# Automatically created on Wed Jul 04 18:06:38 CEST 2012
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
 sourceline 33
  goto body0, after0
 body0:
  assume (!(sA) = emptyset); "assume condition "
 sourceline 37
  skip MARK, 1, ((sA = seqAsSet(seqSub(arrayAsIntSeq($heap, array), i, $heap[array, $array_length]))) & ((i >= 0) & ((i < $heap[array, $array_length]) & (sumA = result)))), 42 ; "marking stone"
 sourceline 38
  havoc xA
  assume (xA :: sA)
 sourceline 39
  sumA := (sumA + xA)
 sourceline 40
  sA := (sA \ singleton(xA))
 sourceline 41
  skip MARK, 2, ((sA = seqAsSet(seqSub(arrayAsIntSeq($heap, array), i, $heap[array, $array_length]))) & ((i >= 0) & (sumA = result))), 42 ; "marking stone"
  goto loop0
 sourceline 33
 after0:
  assume $not((!(sA) = emptyset))
 endOfProgram: 


problem ((setA = seqAsSet(arrayAsIntSeq($heap, array))) & ((!(array) = $null) & (\forall a; (((0 <= a) & (a < $heap[array, $array_length])) -> (\forall b; (((0 <= b) & (b < $heap[array, $array_length])) -> (((($heap[array, $array_index(a)]) as int) = $heap[array, $array_index(b)]) -> (a = b)))))))) |- [0; Java][<0;Sum>]((sumA = $result))
