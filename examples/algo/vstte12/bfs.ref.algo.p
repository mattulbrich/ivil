# Automatically created on Wed Mar 13 01:40:00 CET 2013
include "bfs.decl.p"
include "ref-BFS.minDistance(int,int).p"
function
   int vi('a)
   'a iv(int)

axiom vi_is_positive
   (\T_all 'a; (\forall x as 'a; vi(x) ~~> vi(x) >= 0))

axiom vi_finite_bounded
   (\T_all 'a; (\forall x as 'a; vi(x) ~~> 
       finite(fullset as set('a)) -> vi(x) < card(fullset as set('a))))

axiom vi_injection
   (\T_all 'a; (\forall x as 'a; vi(x) ~~>
       finite(fullset as set('a)) -> iv(vi(x)) = x ))

axiom iv_injection
   (\T_all 'a; (\forall i; iv(i) as 'a~~>
          finite(fullset as set('a)) & 0<=i & i<card(fullset as set('a))
       -> vi(iv(i) as 'a) = i))


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

program bfs source "bfs.ref.algo"
 sourceline 65
  assume finite(((fullset) as set(vertex))) ; "by requirement"
 sourceline 77
  assert finite(((fullset) as set(vertex)))
  size := card(((fullset) as set(vertex)))
 sourceline 79
  V := singleton(src)
 sourceline 80
  C := singleton(src)
 sourceline 81
  N := emptyset
 sourceline 82
  d := 0
 loop0:
 sourceline 97
 sourceline 84
  goto body0, after0
 body0:
  assume !((C = emptyset)); "assume condition "
 sourceline 99
  skip MARK, 1, (((V = (\set v; h[_V, idxBool(vi(v))])) & ((C = (\set v; h[_C, idxBool(vi(v))])) & ((N = (\set v; h[_N, idxBool(vi(v))])) & ((d = _d) & ((vi(src) = _src) & (vi(dest) = _dest)))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & ((arrlen(_V) = size) & ((arrlen(_N) = size) & ((arrlen(_C) = size) & (size = card(((fullset) as set(vertex)))))))))) & (!(C) = emptyset))), pair(0, pair(^((V \ (C \/ N))), tovisit)) ; "marking stone"
 sourceline 101
  havoc v
  assume (v :: C)
 sourceline 104
  C := (C \ singleton(v))
 sourceline 105
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 107
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
 sourceline 108
  assume $not((v = dest)); "else"
 after1:
 sourceline 110
  Vo := V
 sourceline 111
  No := N
 sourceline 113
  tovisit := succ(v)
 loop1:
  goto body1, after2
 body1:
  assume !tovisit= emptyset; "assume condition "
  havoc w; "witness by §(rule deep_update_simplification) §(inst x1 with 'iv(_w) as vertex' hide)"
  assume w :: tovisit ; "choose element in tovisit"
  tovisit := tovisit \ singleton(w)
 sourceline 119
  skip MARK, 2, (((V = (\set v; h[_V, idxBool(vi(v))])) & ((C = (\set v; h[_C, idxBool(vi(v))])) & ((N = (\set v; h[_N, idxBool(vi(v))])) & ((d = _d) & ((vi(src) = _src) & (vi(dest) = _dest)))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & ((arrlen(_V) = size) & ((arrlen(_N) = size) & ((arrlen(_C) = size) & (size = card(((fullset) as set(vertex)))))))))) & ((_v = vi(v)) & ((_w = vi(w)) & (tovisit = ((\set v; (vi(v) > _w)) /\ succ(v))))))), pair(0, pair(^((V \ (C \/ N))), tovisit)) ; "marking stone"
 sourceline 120
  goto then1, else1
 then1:
  assume (!(w) :: V); "then"
 sourceline 122
  V := (V \/ singleton(w))
 sourceline 123
  N := (N \/ singleton(w))
  goto after3
 else1:
 sourceline 124
  assume $not((!(w) :: V)); "else"
 after3:
  goto loop1
 after2:
  assume tovisit= emptyset
 sourceline 127
  goto then2, else2
 then2:
  assume (C = emptyset); "then"
 sourceline 129
  C := N
 sourceline 130
  N := emptyset
 sourceline 131
  d := (d + 1)
  goto after4
 else2:
 sourceline 132
  assume $not((C = emptyset)); "else"
 after4:
  goto loop0
 sourceline 84
 after0:
  assume $not(!((C = emptyset)))
 sourceline 136
  assert (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, ((d + 1) + i)))))) ; "lemma by §(rule int_induction_match)"
 sourceline 139
  assert (\forall j; ((j > d) -> !(minconnect(src, dest, j)))) ; "lemma by §(rule deep_update_simplification nested_quant_z3)"
 sourceline 142
  d := -(1)
 endOfProgram: 


problem ((\forall v; (succ(v) = (\set w; (\exists i; ((0 <= i) & ((i < size) & h[h[h[_this, F_BFS_adjacency], idxRef(vi(v))], idxBool(vi(w))])))))) & ((vi(src) = _src) & ((vi(dest) = _dest) & (finite(((fullset) as set(vertex))) & (card(((fullset) as set(vertex))) = h[_this, F_BFS_size]))))) |- INITIAL_VAR(pair(1, pair(V, V))) -> [0; Java][<0;bfs>]((d = resInt))
