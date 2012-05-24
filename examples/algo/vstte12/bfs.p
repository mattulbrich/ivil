# Automatically created on Thu May 24 13:45:17 CEST 2012
include "bfs.algo.p"
function vertex src 
function vertex dest 
function int d assignable
function int size assignable
function set(vertex) V assignable
function set(vertex) C assignable
function set(vertex) N assignable
function set(vertex) Vo assignable
function set(vertex) Co assignable
function set(vertex) No assignable
function set(vertex) tovisit assignable
function vertex v assignable
function vertex w assignable
function set(vertex) done assignable
function set(vertex) done0 assignable

program bfs source "bfs.algo"
 sourceline 17
  assume finite(((fullset) as set(vertex))) ; "by requirement"
 sourceline 32
  size := card(((fullset) as set(vertex)))
 sourceline 34
  V := singleton(src)
 sourceline 35
  C := singleton(src)
 sourceline 36
  N := emptyset
 sourceline 37
  d := 0
 sourceline 38
  done := emptyset
 loop0:
 sourceline 48
  skip_loopinv ((d >= 0) & ((\forall x; ((x :: C) -> connect(src, x, d))) & ((\forall y; ((y :: N) -> connect(src, y, (d + 1)))) & ((\forall z; ((z :: (V \ N)) <-> (\exists n; (((0 <= n) & (n <= d)) & connect(src, z, n))))) & ((N <: V) & ((done /\ C) = emptyset)))))), ^(done)
 sourceline 39
  goto body0, after0
 body0:
  assume !((C = emptyset)); "assume condition "
 sourceline 50
  assert (\exists v; (v :: C)) ; "assert before choose"
  havoc v
  assume (v :: C)
 sourceline 51
  C := (C \ singleton(v))
 sourceline 52
  done := (done \/ singleton(v))
 sourceline 53
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 55
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
  assume $not((v = dest)); "else"
 sourceline 56
 after1:
 sourceline 58
  Vo := V
 sourceline 59
  No := N
 sourceline 60
  tovisit := succ(v)
 loop1:
  skip_loopinv ((tovisit <: succ(v)) & ((Vo <: V) & ((No <: N) & ((N <: V) & (\forall y; ((y :: N) -> connect(src, y, (d + 1)))))))), tovisit
  goto body1, after2
 body1:
  assume !tovisit= emptyset; "assume condition "
  havoc w
  assume w :: tovisit ; "choose element in tovisit"
  tovisit := tovisit \ singleton(w)
 sourceline 67
  goto then1, else1
 then1:
  assume (!(w) :: V); "then"
 sourceline 69
  V := (V \/ singleton(w))
 sourceline 70
  N := (N \/ singleton(w))
  goto after3
 else1:
  assume $not((!(w) :: V)); "else"
 sourceline 71
 after3:
  goto loop1
 after2:
  assume tovisit= emptyset
 sourceline 74
  goto then2, else2
 then2:
  assume (C = emptyset); "then"
 sourceline 76
  C := N
 sourceline 77
  N := emptyset
 sourceline 78
  d := (d + 1)
  goto after4
 else2:
  assume $not((C = emptyset)); "else"
 sourceline 79
 after4:
  goto loop0
 sourceline 39
 after0:
  assume $not(!((C = emptyset)))
 sourceline 82
  d := -(1)
 endOfProgram: 
 sourceline 20
  assert (d >= -(1)) ; "by ensures"
 sourceline 23
  assert ((d < 0) -> (\forall m; ((m >= 0) -> !(connect(src, dest, m))))) ; "by ensures"
 sourceline 26
  assert ((d >= 0) -> connect(src, dest, d)) ; "by ensures"
 sourceline 29
  assert ((d >= 0) -> (\forall m; (((0 <= m) & (m < d)) -> !(connect(src, dest, m))))) ; "by ensures"

