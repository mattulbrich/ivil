# Automatically created on Thu May 24 20:29:13 CEST 2012
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
 sourceline 29
  size := card(((fullset) as set(vertex)))
 sourceline 31
  V := singleton(src)
 sourceline 32
  C := singleton(src)
 sourceline 33
  N := emptyset
 sourceline 34
  d := 0
 sourceline 35
  done := emptyset
 loop0:
 sourceline 50
  skip_loopinv ((d >= 0) & ((\forall x; ((x :: C) -> minconnect(src, x, d))) & ((\forall y; ((y :: N) -> minconnect(src, y, (d + 1)))) & ((\forall z; ((z :: (V \ N)) <-> (\exists n; (((0 <= n) & (n <= d)) & minconnect(src, z, n))))) & ((\forall a; (minconnect(src, a, (d + 1)) -> ((\exists b; ((a :: succ(b)) & (b :: C))) | (a :: N)))) & (((C = emptyset) -> (N = emptyset)) & ((C <: V) & ((N <: V) & ((!(dest) :: ((V \ N) \ C)) & ((done /\ C) = emptyset)))))))))), ^(done)
 sourceline 36
  goto body0, after0
 body0:
  assume !((C = emptyset)); "assume condition "
 sourceline 52
  assert (\exists v; (v :: C)) ; "assert before choose"
  havoc v
  assume (v :: C)
 sourceline 53
  C := (C \ singleton(v))
 sourceline 54
  done := (done \/ singleton(v))
 sourceline 55
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 57
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
  assume $not((v = dest)); "else"
 sourceline 58
 after1:
 sourceline 60
  Vo := V
 sourceline 61
  No := N
 sourceline 62
  tovisit := succ(v)
 loop1:
  skip_loopinv ((tovisit <: succ(v)) & ((V = (Vo \/ (succ(v) \ tovisit))) & (N = (No \/ (succ(v) \ (V \/ tovisit)))))), tovisit
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
 sourceline 36
 after0:
  assume $not(!((C = emptyset)))
 sourceline 82
  assert (\forall a; !(minconnect(src, a, (d + 1))))
  assume (\forall a; !(minconnect(src, a, (d + 1)))) ; "use lemma"
 sourceline 86
  assert (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, (d + (1 + i))))))) ; " lemma by §(rule int_induction_match)"
  assume (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, (d + (1 + i))))))) ; "use lemma"
 sourceline 89
  d := -(1)
 endOfProgram: 
 sourceline 20
  assert (d >= -(1)) ; "by ensures"
 sourceline 23
  assert ((d < 0) -> (\forall m; ((m >= 0) -> !(minconnect(src, dest, m))))) ; "by ensures"
 sourceline 26
  assert ((d >= 0) -> minconnect(src, dest, d)) ; "by ensures"

