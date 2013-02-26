# Automatically created on Tue Feb 26 13:41:09 CET 2013
include "bfs.decl.p"
include "ref-BFS.minDistance(int,int).p"
function
   int k('a)
   'a inv_k(int)

axiom k_is_positive
   (\T_all 'a; (\forall x as 'a; k(x) ~~> k(x) >= 0))

axiom k_finite_bounded
   (\T_all 'a; (\forall x as 'a; k(x) ~~> 
       finite(fullset as set('a)) -> k(x) < card(fullset as set('a))))

axiom k_injection
   (\T_all 'a; (\forall x as 'a; k(x) ~~>
       finite(fullset as set('a)) -> inv_k(k(x)) = x ))


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
 sourceline 59
  assume finite(((fullset) as set(vertex))) ; "by requirement"
 sourceline 71
  assert finite(((fullset) as set(vertex)))
  size := card(((fullset) as set(vertex)))
 sourceline 73
  V := singleton(src)
 sourceline 74
  C := singleton(src)
 sourceline 75
  N := emptyset
 sourceline 76
  d := 0
 loop0:
 sourceline 91
 sourceline 78
  goto body0, after0
 body0:
  assume !((C = emptyset)); "assume condition "
 sourceline 93
  skip MARK, 1, (((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_V, idxBool(k(v))]))))) & ((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_C, idxBool(k(v))]))))) & ((N = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_N, idxBool(k(v))]))))) & ((d = _d) & ((k(src) = _src) & (k(dest) = _dest)))))) & (!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & ((arrlen(_V) = size) & ((arrlen(_N) = size) & (arrlen(_C) = size))))))), 42 ; "marking stone"
 sourceline 94
  havoc v
  assume (v :: C)
 sourceline 95
  C := (C \ singleton(v))
 sourceline 96
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 98
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
 sourceline 99
  assume $not((v = dest)); "else"
 after1:
 sourceline 101
  Vo := V
 sourceline 102
  No := N
 sourceline 104
  tovisit := succ(v)
 loop1:
  goto body1, after2
 body1:
  assume !tovisit= emptyset; "assume condition "
  havoc w
  assume w :: tovisit ; "choose element in tovisit"
  tovisit := tovisit \ singleton(w)
 sourceline 109
  skip MARK, 2, (((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_V, idxBool(k(v))]))))) & ((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_C, idxBool(k(v))]))))) & ((N = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_N, idxBool(k(v))]))))) & ((d = _d) & ((k(src) = _src) & (k(dest) = _dest)))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & ((arrlen(_V) = size) & ((arrlen(_N) = size) & (arrlen(_C) = size)))))) & ((_v = k(v)) & (_w = k(w))))), 42 ; "marking stone"
 sourceline 110
  goto then1, else1
 then1:
  assume (!(w) :: V); "then"
 sourceline 112
  V := (V \/ singleton(w))
 sourceline 113
  N := (N \/ singleton(w))
  goto after3
 else1:
 sourceline 114
  assume $not((!(w) :: V)); "else"
 after3:
  goto loop1
 after2:
  assume tovisit= emptyset
 sourceline 117
  goto then2, else2
 then2:
  assume (C = emptyset); "then"
 sourceline 119
  C := N
 sourceline 120
  N := emptyset
 sourceline 121
  d := (d + 1)
  goto after4
 else2:
 sourceline 122
  assume $not((C = emptyset)); "else"
 after4:
  goto loop0
 sourceline 78
 after0:
  assume $not(!((C = emptyset)))
  assume (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, ((d + 1) + i)))))) ; "use lemma"
  assume (\forall j; ((j > d) -> !(minconnect(src, dest, j)))) ; "use lemma"
 sourceline 132
  d := -(1)
 endOfProgram: 


problem ((\forall v; (succ(v) = (\set w; (\exists i; ((0 <= i) & ((i < size) & h[h[h[_this, F_BFS_adjacency], idxRef(k(v))], idxBool(k(w))])))))) & ((k(src) = _src) & ((k(dest) = _dest) & (finite(((fullset) as set(vertex))) & (card(((fullset) as set(vertex))) = h[_this, F_BFS_size]))))) |- [0; Java][<0;bfs>]((d = resInt))
