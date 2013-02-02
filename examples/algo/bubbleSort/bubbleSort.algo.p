# Automatically created on Sat Feb 02 15:37:53 CET 2013
include "selectionSort.decl.p"
include "heapAbs.p"
include "$seq.p"
  plugin prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.SeqPrettyPrinter"
  plugin contextExtension : "de.uka.iti.pseudo.gui.extensions.SplitPropositionalExtension"

function seq(int) a 
function int i assignable
function int j assignable
function int t assignable
function int n assignable
function int sum0 assignable
function bool changed assignable
function seq(int) b assignable

program selectionSort source "bubbleSort.algo"
 sourceline 36
  assume (\forall i; (((0 <= i) & (i < seqLen(a))) -> (seqGet(a, i) >= 0))) ; "by requirement"
 sourceline 45
  b := a
 sourceline 46
  i := 0
 sourceline 47
  j := 0
 sourceline 48
  n := seqLen(a)
 sourceline 50
  goto then0, else0
 then0:
  assume (n = 0); "then"
 sourceline 52
  goto endOfProgram ; "Return Statement"
  goto after0
 else0:
 sourceline 53
  assume $not((n = 0)); "else"
 after0:
 loop0:
 sourceline 61
  skip LOOPINV, (((0 <= j) & (j <= n)) & (isPerm(a, b) & ((seqLen(b) = n) & ((\forall k; (\forall l; ((((n - j) <= k) & ((k <= l) & (l < n))) -> (seqGet(b, k) <= seqGet(b, l))))) & (!((j = 0)) -> (\forall k; (((0 <= k) & (k <= (n - j))) -> (seqGet(b, k) <= seqGet(b, (n - j)))))))))), (n - j)
 sourceline 55
  goto body0, after1
 body0:
  assume (j < n); "assume condition "
 sourceline 63
  i := 0
 loop1:
 sourceline 72
  skip LOOPINV, (((0 <= i) & (i <= ((n - j) - 1))) & (isPerm(a, b) & ((seqLen(b) = n) & ((\forall k; (\forall l; ((((n - j) <= k) & ((k <= l) & (l < n))) -> (seqGet(b, k) <= seqGet(b, l))))) & ((\forall k; (((0 <= k) & (k <= i)) -> (seqGet(b, k) <= seqGet(b, i)))) & (!((j = 0)) -> (\forall k; (((0 <= k) & (k <= (n - j))) -> (seqGet(b, k) <= seqGet(b, (n - j))))))))))), (n - i)
 sourceline 64
  goto body1, after2
 body1:
  assume (i < ((n - j) - 1)); "assume condition "
 sourceline 74
  goto then1, else1
 then1:
  assume (seqGet(b, i) > seqGet(b, (i + 1))); "then"
 sourceline 76
  b := seqSwap(b, i, (i + 1))
  goto after3
 else1:
 sourceline 77
  assume $not((seqGet(b, i) > seqGet(b, (i + 1)))); "else"
 after3:
 sourceline 78
  i := (i + 1)
  goto loop1
 sourceline 64
 after2:
  assume $not((i < ((n - j) - 1)))
 sourceline 80
  j := (j + 1)
  goto loop0
 sourceline 55
 after1:
  assume $not((j < n))
 endOfProgram: 
 sourceline 39
  assert isSorted(b) ; "by ensures"
 sourceline 42
  assert isPerm(a, b) ; "by ensures"

