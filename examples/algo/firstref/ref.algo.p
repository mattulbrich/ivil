# Automatically created on Fri Mar 22 16:47:02 CET 2013
include "$int.p"
include "$symbex.p"
include "$decproc.p"
include "$set.p"
include "$seq.p"
include "$refinement.p"
function set(int) setA 
function int sumA assignable
function set(int) sA assignable
function int xA assignable
function seq(int) seqC 
function int sumC assignable
function int i assignable

program A source "ref.algo"
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
  skip MARK, 1, ((sA = seqAsSet(seqSub(seqC, i, seqLen(seqC)))) & ((sumC = sumA) & ((i >= 0) & (i < seqLen(seqC))))), 42 ; "marking stone"
 sourceline 33
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

program C source "ref.algo"
 sourceline 51
  assume (\forall a; (((0 <= a) & (a < seqLen(seqC))) -> (\forall b; (((0 <= b) & (b < seqLen(seqC))) -> ((seqGet(seqC, a) = seqGet(seqC, b)) -> (a = b)))))) ; "by requirement"
 sourceline 54
  i := 0
 sourceline 55
  sumC := 0
 loop0:
 sourceline 57
 sourceline 56
  goto body0, after0
 body0:
  assume (i < seqLen(seqC)); "assume condition "
 sourceline 60
  skip MARK, 1, ((sA = seqAsSet(seqSub(seqC, i, seqLen(seqC)))) & ((sumC = sumA) & ((i >= 0) & (i < seqLen(seqC))))), 42 ; "marking stone"
 sourceline 61
  sumC := (sumC + seqGet(seqC, i))
 sourceline 62
  i := (i + 1)
  goto loop0
 sourceline 56
 after0:
  assume $not((i < seqLen(seqC)))
 endOfProgram: 


problem ((setA = seqAsSet(seqC)) & (\forall a; (((0 <= a) & (a < seqLen(seqC))) -> (\forall b; (((0 <= b) & (b < seqLen(seqC))) -> ((seqGet(seqC, a) = seqGet(seqC, b)) -> (a = b))))))) |- INITIAL_VAR(42) -> [0; C][<0;A>]((sumA = sumC))
