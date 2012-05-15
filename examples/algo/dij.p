# Automatically created on Tue May 08 17:00:38 CEST 2012
include "dij.algo.p"
function node start
function set(node) old_dom_distance assignable
function set(node) dom_distance assignable
function map(node,int) old_distance assignable
function map(node,int) distance assignable
function node n assignable
function node o assignable
function prod(node,node) s assignable
function int d assignable
function set(node) visited assignable
function set(node) nbors0 assignable
function set(node) nbors assignable

program Dij source "dij.algo"
 sourceline 17
  assume (\forall x; (\forall y; (weight(x, y) >= 0)))
 sourceline 19
  dom_distance := emptyset
 sourceline 20
  distance := $store(distance, start, 0)
 sourceline 21
  dom_distance := singleton(start)
 sourceline 23
  visited := emptyset
 loop0:
 sourceline 50
  skip_loopinv ((visited <: dom_distance) & (($load(distance, start) = 0) & ((start :: dom_distance) & ((\forall x; ((x :: dom_distance) -> ($load(distance, x) >= 0))) & ((\forall y; ((y :: (dom_distance \ singleton(start))) -> (\exists x; ((x :: visited) & ((pair(x, y) :: dom_weight) & ($load(distance, y) = ($load(distance, x) + weight(x, y)))))))) & ((\forall a; (\forall b; (((a :: visited) & ((b :: dom_distance) & (pair(a, b) :: dom_weight))) -> ($load(distance, b) <= ($load(distance, a) + weight(a, b)))))) & ((\forall a; (\forall b; (((a :: visited) & (b :: (dom_distance \ visited))) -> ($load(distance, a) <= $load(distance, b))))) & (\forall p; (\forall q; (((p :: visited) & (pair(p, q) :: dom_weight)) -> (q :: dom_distance))))))))))), card((dom_distance \ visited))
 sourceline 26
  goto body0, after0
 body0:
  assume (!(emptyset) = (dom_distance \ visited)); "assume condition "
 sourceline 53
  assert (\exists n; ((!(n) :: visited) & ((n :: dom_distance) & (\forall m; ((m :: (dom_distance \ visited)) -> ($load(distance, n) <= $load(distance, m))))))) ; "assert before choose"
  havoc n
  assume ((!(n) :: visited) & ((n :: dom_distance) & (\forall m; ((m :: (dom_distance \ visited)) -> ($load(distance, n) <= $load(distance, m))))))
 sourceline 59
  visited := (visited \/ singleton(n))
 sourceline 62
  assert (visited <: dom_distance) ; " lemma by §(pick A.3 A.12 S.0)"
 sourceline 67
  old_distance := distance
 sourceline 68
  old_dom_distance := dom_distance
 sourceline 69
  nbors0 := (\set k; ((pair(n, k) :: dom_weight) & (!(k) :: visited)))
 sourceline 70
  nbors := nbors0
 loop1:
 sourceline 107
  skip_loopinv ((nbors <: nbors0) & ((\forall r; ((r :: visited) -> ($load(distance, r) = $load(old_distance, r)))) & ((old_dom_distance <: dom_distance) & ((\forall s; ((s :: old_dom_distance) -> ($load(distance, s) <= $load(old_distance, s)))) & (($load(distance, start) = 0) & ((start :: dom_distance) & ((\forall x; ((x :: dom_distance) -> ($load(distance, x) >= 0))) & ((\forall y; ((y :: (dom_distance \ singleton(start))) -> (\exists x; ((x :: visited) & ((pair(x, y) :: dom_weight) & ($load(distance, y) = ($load(distance, x) + weight(x, y)))))))) & ((\forall b; (((b :: (dom_distance \ nbors)) & (pair(n, b) :: dom_weight)) -> ($load(distance, b) <= ($load(distance, n) + weight(n, b))))) & ((\forall a; (\forall b; (((a :: visited) & (b :: (dom_distance \ visited))) -> ($load(distance, a) <= $load(distance, b))))) & (\forall p; (\forall q; (((p :: visited) & ((!(q) :: nbors) & (pair(p, q) :: dom_weight))) -> (q :: dom_distance)))))))))))))), card(nbors)
 sourceline 72
  goto body1, after1
 body1:
  assume (\exists t; (t :: nbors)); "assume condition "
 sourceline 109
  assert (\exists o; (o :: nbors)) ; "assert before choose"
  havoc o
  assume (o :: nbors)
 sourceline 110
  nbors := (nbors \ singleton(o))
 sourceline 113
  assert (pair(n, o) :: dom_weight)
 sourceline 116
  d := ($load(distance, n) + weight(n, o))
 sourceline 118
  goto then0, else0
 then0:
  assume ((!(o) :: dom_distance) | (d < $load(distance, o))); "then"
 sourceline 120
  dom_distance := (dom_distance \/ singleton(o))
 sourceline 121
  distance := $store(distance, o, d)
  goto after2
 else0:
  assume $not(((!(o) :: dom_distance) | (d < $load(distance, o)))); "else"
 sourceline 122
 after2:
  goto loop1
 sourceline 72
 after1:
  assume $not((\exists t; (t :: nbors)))
  goto loop0
 sourceline 26
 after0:
  assume $not((!(emptyset) = (dom_distance \ visited)))
 sourceline 127
  assert (visited = dom_distance)
 sourceline 128
  assert (\forall y; ((y :: (dom_distance \ singleton(start))) -> (\exists x; ((x :: dom_distance) & ((pair(x, y) :: dom_weight) & ($load(distance, y) = ($load(distance, x) + weight(x, y))))))))
 sourceline 131
  assert (\forall a; ((a :: dom_distance) -> (\forall b; (((b :: dom_distance) & (pair(a, b) :: dom_weight)) -> ($load(distance, b) <= ($load(distance, a) + weight(a, b)))))))


problem 
  finite(fullset as set(node)) |- [[0;Dij]]((true))
