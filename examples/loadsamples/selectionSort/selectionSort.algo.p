# Automatically created on Mon Aug 27 15:18:12 CEST 2012
include "selectionSort.decl.p"
include "$seq.p"
function seq(int) a 
function int i assignable
function int j assignable
function int t assignable
function int n assignable
function seq(int) b assignable

program selectionSort source "selectionSort.algo"
 sourceline 29
  b := a
 sourceline 30
  i := 0
 sourceline 31
  n := seqLen(a)
 sourceline 33
  goto then0, else0
 then0:
  assume (n = 0); "then"
 sourceline 35
  goto endOfProgram ; "Return Statement"
  goto after0
 else0:
 sourceline 36
  assume $not((n = 0)); "else"
 after0:
 loop0:
 sourceline 46
  skip LOOPINV, (((0 <= i) & (i < n)) & ((\forall k; (\forall l; (((0 <= k) & ((k <= l) & (l <= i))) -> (seqGet(b, k) <= seqGet(b, l))))) & ((\forall k; (\forall l; (((0 <= k) & ((k < i) & ((i <= l) & (l < n)))) -> (seqGet(b, k) <= seqGet(b, l))))) & (isPerm(a, b) & (seqLen(b) = n))))), (n - i)
 sourceline 38
  goto body0, after1
 body0:
  assume (i < (n - 1)); "assume condition "
 sourceline 48
  t := i
 sourceline 49
  j := (i + 1)
 loop1:
 sourceline 61
  skip LOOPINV, ((\forall k; (((i <= k) & (k < j)) -> (seqGet(b, t) <= seqGet(b, k)))) & ((\forall k; (\forall l; (((0 <= k) & ((k <= l) & (l <= i))) -> (seqGet(b, k) <= seqGet(b, l))))) & ((\forall k; (\forall l; (((0 <= k) & ((k < i) & ((i <= l) & (l < n)))) -> (seqGet(b, k) <= seqGet(b, l))))) & (((0 <= i) & (i < n)) & ((((i + 1) <= j) & (j <= n)) & (((i <= t) & (t < n)) & (isPerm(a, b) & (seqLen(b) = n)))))))), ((n - j) + 1)
 sourceline 50
  goto body1, after2
 body1:
  assume (j < n); "assume condition "
 sourceline 63
  goto then1, else1
 then1:
  assume (seqGet(b, j) < seqGet(b, t)); "then"
 sourceline 65
  t := j
  goto after3
 else1:
 sourceline 66
  assume $not((seqGet(b, j) < seqGet(b, t))); "else"
 after3:
 sourceline 68
  j := (j + 1)
  goto loop1
 sourceline 50
 after2:
  assume $not((j < n))
 sourceline 71
  b := seqSwap(b, i, t)
 sourceline 73
  i := (i + 1)
  goto loop0
 sourceline 38
 after1:
  assume $not((i < (n - 1)))
 endOfProgram: 
 sourceline 23
  assert isSorted(b) ; "by ensures"
 sourceline 26
  assert isPerm(a, b) ; "by ensures"

