# Automatically created on Tue May 29 20:22:52 CEST 2012
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

program bfs source "bfs.algo"
 sourceline 16
  assume finite(((fullset) as set(vertex))) ; "by requirement"
 sourceline 28
  size := card(((fullset) as set(vertex)))
 sourceline 30
  V := singleton(src)
 sourceline 31
  C := singleton(src)
 sourceline 32
  N := emptyset
 sourceline 33
  d := 0
 loop0:
 sourceline 48
  skip_loopinv ((d >= 0) & ((\forall x; ((x :: C) -> minconnect(src, x, d))) & ((\forall y; ((y :: N) <-> (\exists q; (minconnect(src, q, d) & ((!(q) :: C) & (y :: succ(q))))))) & ((\forall z; ((z :: (V \ N)) <-> (\exists n; (((0 <= n) & (n <= d)) & minconnect(src, z, n))))) & ((\forall a; (minconnect(src, a, (d + 1)) -> ((\exists c; ((a :: succ(c)) & (c :: C))) | (a :: N)))) & (((C = emptyset) -> (N = emptyset)) & ((C <: V) & ((N <: V) & (!(dest) :: ((V \ N) \ C)))))))))), ^((V \ (C \/ N)))
 sourceline 34
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
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 54
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
  assume $not((v = dest)); "else"
 sourceline 55
 after1:
 sourceline 57
  Vo := V
 sourceline 58
  No := N
 sourceline 70
  V := (Vo \/ succ(v))
 sourceline 71
  N := (No \/ (succ(v) \ Vo))
 sourceline 73
  goto then1, else1
 then1:
  assume (C = emptyset); "then"
 sourceline 75
  C := N
 sourceline 76
  N := emptyset
 sourceline 77
  d := (d + 1)
  goto after2
 else1:
  assume $not((C = emptyset)); "else"
 sourceline 78
 after2:
  goto loop0
 sourceline 34
 after0:
  assume $not(!((C = emptyset)))
 sourceline 82
  assert (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, (d + (1 + i))))))) ; " lemma by §(rule int_induction_match)"
  assume (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, (d + (1 + i))))))) ; "use lemma"
 sourceline 85
  d := -(1)
 endOfProgram: 
 sourceline 19
  assert (d >= -(1)) ; "by ensures"
 sourceline 22
  assert ((d < 0) -> (\forall m; ((m >= 0) -> !(minconnect(src, dest, m))))) ; "by ensures"
 sourceline 25
  assert ((d >= 0) -> minconnect(src, dest, d)) ; "by ensures"

