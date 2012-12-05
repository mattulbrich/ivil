# Automatically created on Wed Dec 05 18:02:09 CET 2012
include "bfs.decl.p"
include "ref-BFS.minDistance(int,int).p"
function
   int k(vertex) unique

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
 sourceline 44
  assume finite(((fullset) as set(vertex))) ; "by requirement"
 sourceline 56
  size := card(((fullset) as set(vertex)))
 sourceline 58
  V := singleton(src)
 sourceline 59
  C := singleton(src)
 sourceline 60
  N := emptyset
 sourceline 61
  d := 0
 loop0:
 sourceline 76
 sourceline 63
  goto body0, after0
 body0:
  assume !((C = emptyset)); "assume condition "
 sourceline 78
  skip MARK, 1, (((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_V, idxBool(k(v))]))))) & ((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_C, idxBool(k(v))]))))) & ((N = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_N, idxBool(k(v))]))))) & ((d = _d) & ((k(src) = _src) & (k(dest) = _dest)))))) & (!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & ((arrlen(_V) = size) & ((arrlen(_N) = size) & (arrlen(_C) = size))))))), 42 ; "marking stone"
 sourceline 79
  havoc v
  assume (v :: C)
 sourceline 80
  C := (C \ singleton(v))
 sourceline 81
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 83
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
 sourceline 84
  assume $not((v = dest)); "else"
 after1:
 sourceline 86
  Vo := V
 sourceline 87
  No := N
 sourceline 89
  tovisit := succ(v)
 loop1:
  goto body1, after2
 body1:
  assume !tovisit= emptyset; "assume condition "
  havoc w
  assume w :: tovisit ; "choose element in tovisit"
  tovisit := tovisit \ singleton(w)
 sourceline 94
  skip MARK, 2, (((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_V, idxBool(k(v))]))))) & ((V = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_C, idxBool(k(v))]))))) & ((N = (\set v; (\exists i; ((0 <= i) & ((i < size) & h[_N, idxBool(k(v))]))))) & ((d = _d) & ((k(src) = _src) & (k(dest) = _dest)))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & ((arrlen(_V) = size) & ((arrlen(_N) = size) & (arrlen(_C) = size)))))) & ((_v = k(v)) & (_w = k(w))))), 42 ; "marking stone"
 sourceline 95
  goto then1, else1
 then1:
  assume (!(w) :: V); "then"
 sourceline 97
  V := (V \/ singleton(w))
 sourceline 98
  N := (N \/ singleton(w))
  goto after3
 else1:
 sourceline 99
  assume $not((!(w) :: V)); "else"
 after3:
  goto loop1
 after2:
  assume tovisit= emptyset
 sourceline 102
  goto then2, else2
 then2:
  assume (C = emptyset); "then"
 sourceline 104
  C := N
 sourceline 105
  N := emptyset
 sourceline 106
  d := (d + 1)
  goto after4
 else2:
 sourceline 107
  assume $not((C = emptyset)); "else"
 after4:
  goto loop0
 sourceline 63
 after0:
  assume $not(!((C = emptyset)))
  assume (\forall i; ((i >= 0) -> (\forall a; !(minconnect(src, a, ((d + 1) + i)))))) ; "use lemma"
  assume (\forall j; ((j > d) -> !(minconnect(src, dest, j)))) ; "use lemma"
 sourceline 117
  d := -(1)
 endOfProgram: 


problem ((\forall v; (succ(v) = (\set w; (\exists i; ((0 <= i) & ((i < size) & h[h[h[_this, F_BFS_adjacency], idxRef(k(v))], idxBool(k(w))])))))) & ((k(src) = _src) & ((k(dest) = _dest) & (fullset = (\set v; ((0 <= k(v)) & (k(v) < size))))))) |- [0; Java][<0;bfs>]((d = resInt))
