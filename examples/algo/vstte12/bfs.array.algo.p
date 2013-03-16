# Automatically created on Sat Mar 16 11:06:21 CET 2013
include "bfs.decl.p"
include "$refinement.p"
include "$seq.p"
plugin
  contextExtension: "de.uka.iti.pseudo.gui.extensions.OopsExt"

function
   int vi(vertex)
   vertex iv(int)

axiom vi_is_positive
   (\forall x as vertex; vi(x) ~~> vi(x) >= 0)

axiom vi_finite_bounded
   (\forall x as vertex; vi(x) ~~> 
       finite(fullset as set(vertex)) -> vi(x) < card(fullset as set(vertex)))

axiom vi_injection
   (\forall x as vertex; vi(x) ~~>
       finite(fullset as set(vertex)) -> iv(vi(x)) = x)

axiom iv_injection
   (\forall i; iv(i) ~~>
          finite(fullset as set(vertex)) & 0<=i & i<card(fullset as set(vertex))
       -> vi(iv(i)) = i)


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
function int a_src 
function int a_dest 
function int a_d assignable
function int a_size assignable
function seq(bool) a_V assignable
function seq(bool) a_C assignable
function seq(bool) a_N assignable
function int a_v assignable
function int a_w assignable
function int a_t assignable

program bfs source "bfs.array.algo"
 sourceline 50
  assume finite(((fullset) as set(vertex))) ; "by requirement"
 sourceline 63
  V := singleton(src)
 sourceline 64
  C := singleton(src)
 sourceline 65
  N := emptyset
 sourceline 66
  d := 0
 loop0:
 sourceline 71
 sourceline 70
  goto body0, after0
 body0:
  assume !((C = emptyset)); "assume condition "
 sourceline 74
  skip MARK, 1, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & !((C = emptyset))), 0 ; "marking stone"
 sourceline 76
  havoc v
  assume (v :: C)
 sourceline 77
  C := (C \ singleton(v))
 sourceline 78
  goto then0, else0
 then0:
  assume (v = dest); "then"
 sourceline 80
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
 sourceline 81
  assume $not((v = dest)); "else"
 after1:
 sourceline 83
  skip MARK, 2, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & (vi(v) = a_v)), 0 ; "marking stone"
 sourceline 85
  tovisit := succ(v)
 loop1:
  goto body1, after2
 body1:
  assume !tovisit= emptyset; "assume condition "
  havoc w
  assume w :: tovisit ; "choose element in tovisit"
  tovisit := tovisit \ singleton(w)
 sourceline 88
  skip MARK, 3, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & ((vi(v) = a_v) & ((vi(w) = a_w) & (tovisit = ((\set x ; (vi(x) > a_w)) /\ succ(v)))))), 0 ; "marking stone"
 sourceline 89
  goto then1, else1
 then1:
  assume (!(w) :: V); "then"
 sourceline 91
  V := (V \/ singleton(w))
 sourceline 92
  N := (N \/ singleton(w))
  goto after3
 else1:
 sourceline 93
  assume $not((!(w) :: V)); "else"
 after3:
 sourceline 94
  skip MARK, 4, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & ((vi(v) = a_v) & ((vi(w) = a_w) & (tovisit = ((\set x ; (vi(x) > a_w)) /\ succ(v)))))), 0 ; "marking stone"
  goto loop1
 after2:
  assume tovisit= emptyset
 sourceline 97
  skip MARK, 5, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & (vi(v) = a_v)), 0 ; "marking stone"
 sourceline 99
  goto then2, else2
 then2:
  assume (C = emptyset); "then"
 sourceline 101
  C := N
 sourceline 102
  N := emptyset
 sourceline 103
  d := (d + 1)
  goto after4
 else2:
 sourceline 104
  assume $not((C = emptyset)); "else"
 after4:
 sourceline 106
  skip MARK, 6, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & (vi(v) = a_v)), 0 ; "marking stone"
  goto loop0
 sourceline 70
 after0:
  assume $not(!((C = emptyset)))
 sourceline 110
  d := -(1)
 endOfProgram: 

program bfs_array source "bfs.array.algo"
 sourceline 124
  a_V := (\seqDef i; 0; a_size; (i = a_src))
 sourceline 125
  a_C := (\seqDef i; 0; a_size; (i = a_src))
 sourceline 126
  a_N := (\seqDef i; 0; a_size; false)
 sourceline 127
  a_d := 0
 loop0:
 sourceline 130
 sourceline 129
  goto body0, after0
 body0:
  assume (\exists k; (((0 <= k) & (k < a_size)) & seqGet(a_C, k))); "assume condition "
 sourceline 132
  skip MARK, 1, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & !((C = emptyset))), 0 ; "marking stone"
 sourceline 134
  havoc a_v
  assume (((0 <= a_v) & (a_v < a_size)) & seqGet(a_C, a_v))
 sourceline 135
  a_C := seqUpdate(a_C, a_v, false)
 sourceline 137
  goto then0, else0
 then0:
  assume (a_v = a_dest); "then"
 sourceline 139
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
 sourceline 140
  assume $not((a_v = a_dest)); "else"
 after1:
 sourceline 142
  skip MARK, 2, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & (vi(v) = a_v)), 0 ; "marking stone"
 sourceline 143
  a_w := 0
 loop1:
 sourceline 145
 sourceline 144
  goto body1, after2
 body1:
  assume (\exists i; (((a_w <= i) & (i < a_size)) & seqGet(a_succ(a_v), i))); "assume condition "
 sourceline 147
  a_t := a_w
 sourceline 148
  havoc a_w
  assume (((a_t <= a_w) & (a_w < a_size)) & (seqGet(a_succ(a_v), a_w) & (\forall j; (((a_t <= j) & (j < a_w)) -> !(seqGet(a_succ(a_v), a_w))))))
 sourceline 151
  skip MARK, 3, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & ((vi(v) = a_v) & ((vi(w) = a_w) & (tovisit = ((\set x ; (vi(x) > a_w)) /\ succ(v)))))), 0 ; "marking stone"
 sourceline 153
  goto then1, else1
 then1:
  assume !(seqGet(a_V, a_w)); "then"
 sourceline 155
  a_V := seqUpdate(a_V, a_w, true)
 sourceline 156
  a_N := seqUpdate(a_N, a_w, true)
  goto after3
 else1:
 sourceline 157
  assume $not(!(seqGet(a_V, a_w))); "else"
 after3:
 sourceline 159
  skip MARK, 4, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & ((vi(v) = a_v) & ((vi(w) = a_w) & (tovisit = ((\set x ; (vi(x) > a_w)) /\ succ(v)))))), 0 ; "marking stone"
 sourceline 160
  a_w := (a_w + 1)
  goto loop1
 sourceline 144
 after2:
  assume $not((\exists i; (((a_w <= i) & (i < a_size)) & seqGet(a_succ(a_v), i))))
 sourceline 163
  skip MARK, 5, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & (vi(v) = a_v)), 0 ; "marking stone"
 sourceline 165
  goto then2, else2
 then2:
  assume !((\exists i; (((0 <= i) & (i < a_size)) & seqGet(a_C, i)))); "then"
 sourceline 167
  a_C := a_N
 sourceline 168
  a_N := (\seqDef i; 0; a_size; false)
 sourceline 169
  a_d := (a_d + 1)
  goto after4
 else2:
 sourceline 170
  assume $not(!((\exists i; (((0 <= i) & (i < a_size)) & seqGet(a_C, i))))); "else"
 after4:
 sourceline 172
  skip MARK, 6, (((C = (\set x; seqGet(a_C, vi(x)))) & ((seqLen(a_C) = size) & ((N = (\set x; seqGet(a_N, vi(x)))) & ((seqLen(a_N) = size) & ((V = (\set x; seqGet(a_V, vi(x)))) & ((seqLen(a_V) = size) & (d = a_d))))))) & (vi(v) = a_v)), 0 ; "marking stone"
  goto loop0
 sourceline 129
 after0:
  assume $not((\exists k; (((0 <= k) & (k < a_size)) & seqGet(a_C, k))))
 sourceline 176
  a_d := -(1)
 endOfProgram: 


problem (finite(((fullset) as set(vertex))) & ((card(((fullset) as set(vertex))) = size) & ((a_size = size) & ((a_src = vi(src)) & ((a_dest = vi(dest)) & (\forall v; (succ(v) = (\set x; seqGet(a_succ(vi(v)), vi(x)))))))))) |- INITIAL_VAR(0) -> [0; bfs_array][<0;bfs>]((d = a_d))
