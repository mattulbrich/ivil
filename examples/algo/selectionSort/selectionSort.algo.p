# Automatically created on Fri Jun 01 00:06:46 CEST 2012
include "selectionSort.decl.p"
function array(int) a 
function int i assignable
function int j assignable
function int t assignable
function int n assignable
function array(int) b assignable

program selectionSort source "selectionSort.algo"
 sourceline 25
  b := a
 sourceline 26
  i := 1
 sourceline 27
  n := length(a)
 sourceline 29
  goto then0, else0
 then0:
  assume (n = 0); "then"
 sourceline 31
  goto endOfProgram ; "Return Statement"
  goto after0
 else0:
 sourceline 32
  assume $not((n = 0)); "else"
 after0:
 loop0:
 sourceline 42
  skip_loopinv ((i >= 1) & ((i <= n) & ((\forall k; (\forall l; (((1 <= k) & ((k <= l) & (l <= i))) -> (read(b, k) <= read(b, l))))) & ((\forall k; (\forall l; (((1 <= k) & ((k < i) & ((i <= l) & (l <= n)))) -> (read(b, k) <= read(b, l))))) & (isPerm(a, b) & (length(b) = n)))))), (n - i)
 sourceline 34
  goto body0, after1
 body0:
  assume (i < n); "assume condition "
 sourceline 44
  t := i
 sourceline 45
  j := (i + 1)
 loop1:
 sourceline 57
  skip_loopinv ((\forall k; (((i <= k) & (k < j)) -> (read(b, t) <= read(b, k)))) & ((\forall k; (\forall l; (((1 <= k) & ((k <= l) & (l <= i))) -> (read(b, k) <= read(b, l))))) & ((\forall k; (\forall l; (((1 <= k) & ((k < i) & ((i <= l) & (l <= n)))) -> (read(b, k) <= read(b, l))))) & ((1 <= i) & ((i < n) & (((i + 1) <= j) & ((j <= (n + 1)) & ((i <= t) & ((t <= n) & (isPerm(a, b) & (length(b) = n))))))))))), ((n - j) + 1)
 sourceline 46
  goto body1, after2
 body1:
  assume (j <= n); "assume condition "
 sourceline 59
  goto then1, else1
 then1:
  assume (read(b, j) < read(b, t)); "then"
 sourceline 61
  t := j
  goto after3
 else1:
 sourceline 62
  assume $not((read(b, j) < read(b, t))); "else"
 after3:
 sourceline 63
  j := (j + 1)
  goto loop1
 sourceline 46
 after2:
  assume $not((j <= n))
 sourceline 66
  b := swap(b, i, t)
 sourceline 67
  i := (i + 1)
  goto loop0
 sourceline 34
 after1:
  assume $not((i < n))
 endOfProgram: 
 sourceline 19
  assert isSorted(b) ; "by ensures"
 sourceline 22
  assert isPerm(a, b) ; "by ensures"

