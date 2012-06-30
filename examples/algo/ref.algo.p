# Automatically created on Sat Jun 30 23:35:24 CEST 2012
include "$refinement.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"
include "$set.p"
include "$seq.p"
function set(int) setA 
function int sumA assignable
function set(int) sA assignable
function int xA assignable
function seq(int) seqC 
function int sumC assignable
function int i assignable

program A source "ref.algo"
 sourceline 27
  sumA := 0
 sourceline 28
  sA := setA
 loop0:
 sourceline 30
 sourceline 29
  goto body0, after0
 body0:
  assume (!(sA) = emptyset); "assume condition "
 sourceline 33
  havoc xA
  assume (xA :: sA)
 sourceline 34
  sumA := (sumA + xA)
 sourceline 35
  sA := (sA \ singleton(xA))
 sourceline 36
  skip MARK, 1, ((sA = seqAsSet(seqSub(seqC, i, seqLen(seqC)))) & ((sumC = sumA) & (\forall a; (((0 <= a) & (a < seqLen(seqC))) -> (\forall b; (((0 <= b) & (b < seqLen(seqC))) -> ((seqGet(seqC, a) = seqGet(seqC, b)) -> (a = b)))))))), 42 ; "marking stone"
  goto loop0
 sourceline 29
 after0:
  assume $not((!(sA) = emptyset))
 endOfProgram: 

program C source "ref.algo"
 sourceline 52
  assume (\forall a; (((0 <= a) & (a < seqLen(seqC))) -> (\forall b; (((0 <= b) & (b < seqLen(seqC))) -> ((seqGet(seqC, a) = seqGet(seqC, b)) -> (a = b)))))) ; "by requirement"
 sourceline 55
  i := 0
 sourceline 56
  sumC := 0
 loop0:
 sourceline 58
 sourceline 57
  goto body0, after0
 body0:
  assume (i < seqLen(seqC)); "assume condition "
 sourceline 61
  sumC := (sumC + seqGet(seqC, i))
 sourceline 62
  i := (i + 1)
 sourceline 63
  skip MARK, 1, ((sA = seqAsSet(seqSub(seqC, i, seqLen(seqC)))) & ((sumC = sumA) & (\forall a; (((0 <= a) & (a < seqLen(seqC))) -> (\forall b; (((0 <= b) & (b < seqLen(seqC))) -> ((seqGet(seqC, a) = seqGet(seqC, b)) -> (a = b)))))))), 42 ; "marking stone"
  goto loop0
 sourceline 57
 after0:
  assume $not((i < seqLen(seqC)))
 endOfProgram: 


problem (setA = seqAsSet(seqC)) |- [0; C][<0;A>]((sumA = sumC))
