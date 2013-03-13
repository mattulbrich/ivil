# Automatically created on Wed Mar 13 00:56:42 CET 2013
include "dij.decl.p"
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
 sourceline 9
  assume finite(((fullset) as set(node))) ; "by requirement"
 sourceline 12
  assume (\forall x; (\forall y; (weight(x, y) >= 0))) ; "by requirement"
 sourceline 43
  dom_distance := emptyset
 sourceline 44
  distance := $store(distance, start, 0)
 sourceline 45
  dom_distance := singleton(start)
 sourceline 47
  visited := emptyset
 loop0:
 sourceline 74
  skip LOOPINV, ((visited <: dom_distance) & (($load(distance, start) = 0) & ((start :: dom_distance) & ((\forall x; ((x :: dom_distance) -> ($load(distance, x) >= 0))) & ((\forall y; ((y :: (dom_distance \ singleton(start))) -> (\exists x; ((x :: visited) & ((pair(x, y) :: dom_weight) & ($load(distance, y) = ($load(distance, x) + weight(x, y)))))))) & ((\forall a; (\forall b; (((a :: visited) & ((b :: dom_distance) & (pair(a, b) :: dom_weight))) -> ($load(distance, b) <= ($load(distance, a) + weight(a, b)))))) & ((\forall a; (\forall b; (((a :: visited) & (b :: (dom_distance \ visited))) -> ($load(distance, a) <= $load(distance, b))))) & (\forall p; (\forall q; (((p :: visited) & (pair(p, q) :: dom_weight)) -> (q :: dom_distance))))))))))), ^(visited)
 sourceline 50
  goto body0, after0
 body0:
  assume (!(emptyset) = (dom_distance \ visited)); "assume condition "
 sourceline 77
  assert (\exists n; ((!(n) :: visited) & ((n :: dom_distance) & (\forall m; ((m :: (dom_distance \ visited)) -> ($load(distance, n) <= $load(distance, m))))))) ; "assert existence"
  havoc n
  assume ((!(n) :: visited) & ((n :: dom_distance) & (\forall m; ((m :: (dom_distance \ visited)) -> ($load(distance, n) <= $load(distance, m))))))
 sourceline 83
  visited := (visited \/ singleton(n))
 sourceline 86
  assert (visited <: dom_distance)
  assume (visited <: dom_distance) ; "use lemma"
 sourceline 91
  old_distance := distance
 sourceline 92
  old_dom_distance := dom_distance
 sourceline 93
  nbors0 := (\set k; ((pair(n, k) :: dom_weight) & (!(k) :: visited)))
 sourceline 94
  nbors := nbors0
 loop1:
 sourceline 131
  skip LOOPINV, ((nbors <: nbors0) & ((\forall r; ((r :: visited) -> ($load(distance, r) = $load(old_distance, r)))) & ((old_dom_distance <: dom_distance) & ((\forall s; ((s :: old_dom_distance) -> ($load(distance, s) <= $load(old_distance, s)))) & (($load(distance, start) = 0) & ((start :: dom_distance) & ((\forall x; ((x :: dom_distance) -> ($load(distance, x) >= 0))) & ((\forall y; ((y :: (dom_distance \ singleton(start))) -> (\exists x; ((x :: visited) & ((pair(x, y) :: dom_weight) & ($load(distance, y) = ($load(distance, x) + weight(x, y)))))))) & ((\forall b; (((b :: (dom_distance \ nbors)) & (pair(n, b) :: dom_weight)) -> ($load(distance, b) <= ($load(distance, n) + weight(n, b))))) & ((\forall a; (\forall b; (((a :: visited) & (b :: (dom_distance \ visited))) -> ($load(distance, a) <= $load(distance, b))))) & (\forall p; (\forall q; (((p :: visited) & ((!(q) :: nbors) & (pair(p, q) :: dom_weight))) -> (q :: dom_distance)))))))))))))), nbors
 sourceline 96
  goto body1, after1
 body1:
  assume (\exists t; (t :: nbors)); "assume condition "
 sourceline 133
  assert (\exists o; (o :: nbors)) ; "assert existence"
  havoc o
  assume (o :: nbors)
 sourceline 134
  nbors := (nbors \ singleton(o))
 sourceline 137
  assert (pair(n, o) :: dom_weight)
 sourceline 140
  d := ($load(distance, n) + weight(n, o))
 sourceline 142
  goto then0, else0
 then0:
  assume ((!(o) :: dom_distance) | (d < $load(distance, o))); "then"
 sourceline 144
  dom_distance := (dom_distance \/ singleton(o))
 sourceline 145
  distance := $store(distance, o, d)
  goto after2
 else0:
 sourceline 146
  assume $not(((!(o) :: dom_distance) | (d < $load(distance, o)))); "else"
 after2:
  goto loop1
 sourceline 96
 after1:
  assume $not((\exists t; (t :: nbors)))
  goto loop0
 sourceline 50
 after0:
  assume $not((!(emptyset) = (dom_distance \ visited)))
 endOfProgram: 
 sourceline 14
  assert (visited = dom_distance) ; "by ensures"
 sourceline 16
  assert (\forall y; ((y :: (dom_distance \ singleton(start))) -> (\exists x; ((x :: dom_distance) & ((pair(x, y) :: dom_weight) & ($load(distance, y) = ($load(distance, x) + weight(x, y)))))))) ; "by ensures"
 sourceline 20
  assert (\forall a; ((a :: dom_distance) -> (\forall b; (((b :: dom_distance) & (pair(a, b) :: dom_weight)) -> ($load(distance, b) <= ($load(distance, a) + weight(a, b))))))) ; "by ensures"
 sourceline 24
  assert (\forall p; (\forall q; (((p :: dom_distance) & (pair(p, q) :: dom_weight)) -> (q :: dom_distance)))) ; "by ensures"

