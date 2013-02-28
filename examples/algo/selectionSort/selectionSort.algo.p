# Automatically created on Thu Feb 28 23:34:09 CET 2013
include "selectionSort.decl.p"
include "heapAbs.p"
include "SelSort.sort(int[]).p"
include "$seq.p"
  plugin prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.SeqPrettyPrinter"

function seq(int) a 
function int i assignable
function int j assignable
function int t assignable
function int n assignable
function seq(int) b assignable

program selectionSort source "selectionSort.algo"
 sourceline 40
  b := a
 sourceline 41
  i := 0
 sourceline 42
  n := seqLen(a)
 sourceline 44
  goto then0, else0
 then0:
  assume (n = 0); "then"
 sourceline 46
  goto endOfProgram ; "Return Statement"
  goto after0
 else0:
 sourceline 47
  assume $not((n = 0)); "else"
 after0:
 loop0:
 sourceline 57
  skip LOOPINV, (((0 <= i) & (i < n)) & ((\forall k; (\forall l; (((0 <= k) & ((k <= l) & (l <= i))) -> (seqGet(b, k) <= seqGet(b, l))))) & ((\forall k; (\forall l; (((0 <= k) & ((k < i) & ((i <= l) & (l < n)))) -> (seqGet(b, k) <= seqGet(b, l))))) & (isPerm(a, b) & (seqLen(b) = n))))), (n - i)
 sourceline 49
  goto body0, after1
 body0:
  assume (i < (n - 1)); "assume condition "
 sourceline 59
  t := i
 sourceline 60
  j := (i + 1)
 loop1:
 sourceline 72
  skip LOOPINV, ((\forall k; (((i <= k) & (k < j)) -> (seqGet(b, t) <= seqGet(b, k)))) & ((\forall k; (\forall l; (((0 <= k) & ((k <= l) & (l <= i))) -> (seqGet(b, k) <= seqGet(b, l))))) & ((\forall k; (\forall l; (((0 <= k) & ((k < i) & ((i <= l) & (l < n)))) -> (seqGet(b, k) <= seqGet(b, l))))) & (((0 <= i) & (i < n)) & ((((i + 1) <= j) & (j <= n)) & (((i <= t) & (t < n)) & (isPerm(a, b) & (seqLen(b) = n)))))))), ((n - j) + 1)
 sourceline 61
  goto body1, after2
 body1:
  assume (j < n); "assume condition "
 sourceline 74
  goto then1, else1
 then1:
  assume (seqGet(b, j) < seqGet(b, t)); "then"
 sourceline 76
  t := j
  goto after3
 else1:
 sourceline 77
  assume $not((seqGet(b, j) < seqGet(b, t))); "else"
 after3:
 sourceline 79
  j := (j + 1)
  goto loop1
 sourceline 61
 after2:
  assume $not((j < n))
 sourceline 82
  b := seqSwap(b, i, t)
 sourceline 84
  i := (i + 1)
  goto loop0
 sourceline 49
 after1:
  assume $not((i < (n - 1)))
 endOfProgram: 
 sourceline 34
  assert isSorted(b) ; "by ensures"
 sourceline 37
  assert isPerm(a, b) ; "by ensures"

