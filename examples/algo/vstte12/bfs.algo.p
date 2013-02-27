# Automatically created on Wed Dec 05 20:29:30 CET 2012
include "bfs.decl.p"
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
  skip LOOPINV, ((d >= 0) & ((\forall x; ((x :: C) -> minconnect(src, x, d))) & ((\forall y; ((y :: N) <-> (\exists q; (minconnect(src, q, d) & ((!(q) :: C) & (y :: succ(q))))))) & ((\forall z; ((z :: (V \ N)) <-> (\exists n; (((0 <= n) & (n <= d)) & minconnect(src, z, n))))) & ((\forall a; (minconnect(src, a, (d + 1)) -> ((\exists c; ((a :: succ(c)) & (c :: C))) | (a :: N)))) & (((C = emptyset) -> (N = emptyset)) & ((C <: V) & ((N <: V) & (!(dest) :: ((V \ N) \ C)))))))))), ^((V \ (C \/ N)))
 sourceline 35
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
 sourceline 55
  assume $not((v = dest)); "else"
 after1:
 sourceline 57
  Vo := V
 sourceline 58
  No := N
 sourceline 60
  tovisit := succ(v)
 loop1:
  skip LOOPINV, ((tovisit <: succ(v)) & ((V = (Vo \/ (succ(v) \ tovisit))) & (N = (No \/ (succ(v) \ (Vo \/ tovisit)))))), tovisit
  goto body1, after2
 body1:
  assume !tovisit= emptyset; "assume condition "
  havoc w
  assume w :: tovisit ; "choose element in tovisit"
  tovisit := tovisit \ singleton(w)
 sourceline 65
  goto then1, else1
 then1:
  assume (!(w) :: V); "then"
 sourceline 67
  V := (V \/ singleton(w))
 sourceline 68
  N := (N \/ singleton(w))
  goto after3
 else1:
 sourceline 69
  assume $not((!(w) :: V)); "else"
 after3:
  goto loop1
 after2:
  assume tovisit= emptyset
 sourceline 72
  goto then2, else2
 then2:
  assume (C = emptyset); "then"
 sourceline 74
  C := N
 sourceline 75
  N := emptyset
 sourceline 76
  d := (d + 1)
  goto after4
 else2:
 sourceline 77
  assume $not((C = emptyset)); "else"
 after4:
  goto loop0
 sourceline 35
 after0:
  assume $not(!((C = emptyset)))
 sourceline 81
  assert (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, ((d + 1) + i)))))) ; " lemma by §(rule int_induction_match)"
  assume (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, ((d + 1) + i)))))) ; "use lemma"
 sourceline 84
  assert (\forall j; ((j > d) -> !(minconnect(src, dest, j)))) ; " lemma by §(rule deep_update_simplification nested_quant_z3)"
  assume (\forall j; ((j > d) -> !(minconnect(src, dest, j)))) ; "use lemma"
 sourceline 87
  d := -(1)
 endOfProgram: 
 sourceline 19
  assert (d >= -(1)) ; "by ensures"
 sourceline 22
  assert ((d < 0) -> (\forall m; ((m >= 0) -> !(minconnect(src, dest, m))))) ; "by ensures"
 sourceline 25
  assert ((d >= 0) -> minconnect(src, dest, d)) ; "by ensures"

