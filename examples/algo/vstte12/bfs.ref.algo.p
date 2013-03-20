# Automatically created on Wed Mar 20 10:48:31 CET 2013
include "bfs.decl.p"
include "ref-BFS.minDistance(int,int).p"
plugin
  contextExtension: "de.uka.iti.pseudo.gui.extensions.OopsExt"

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

program bfs_array source "bfs.ref.algo"
 sourceline 53
  a_V := (\seqDef i; 0; a_size; (i = a_src))
 sourceline 54
  a_C := (\seqDef i; 0; a_size; (i = a_src))
 sourceline 55
  a_N := (\seqDef i; 0; a_size; false)
 sourceline 56
  a_d := 0
 loop0:
 sourceline 59
 sourceline 58
  goto body0, after0
 body0:
  assume (\exists k; (((0 <= k) & (k < a_size)) & seqGet(a_C, k))); "assume condition "
 sourceline 61
  skip MARK, 1, (((a_V = boolArrAsSeq(h, _V)) & ((a_C = boolArrAsSeq(h, _C)) & ((a_N = boolArrAsSeq(h, _N)) & ((a_d = _d) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size]))))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & (!((_V = h[_this, F_BFS_adjacency])) & (!((_N = h[_this, F_BFS_adjacency])) & (!((_C = h[_this, F_BFS_adjacency])) & (!((_V = _this)) & (!((_N = _this)) & (!((_C = _this)) & ((\forall i; (((0 <= i) & (i < a_size)) -> (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _V)) & (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _N)) & !((h[h[_this, F_BFS_adjacency], idxRef(i)] = _C)))))) & ((arrlen(_V) = a_size) & ((arrlen(_N) = a_size) & (arrlen(_C) = a_size))))))))))))) & (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & (\exists i; (((0 <= i) & (i < arrlen(_C))) & h[_C, idxBool(i)]))))), 0 ; "marking stone"
 sourceline 63
  havoc a_v
  assume (((0 <= a_v) & (a_v < a_size)) & seqGet(a_C, a_v))
 sourceline 64
  a_C := seqUpdate(a_C, a_v, false)
 sourceline 66
  goto then0, else0
 then0:
  assume (a_v = a_dest); "then"
 sourceline 68
  goto endOfProgram ; "Return Statement"
  goto after1
 else0:
 sourceline 69
  assume $not((a_v = a_dest)); "else"
 after1:
 sourceline 71
  skip MARK, 2, (((a_V = boolArrAsSeq(h, _V)) & ((a_C = boolArrAsSeq(h, _C)) & ((a_N = boolArrAsSeq(h, _N)) & ((a_d = _d) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size]))))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & (!((_V = h[_this, F_BFS_adjacency])) & (!((_N = h[_this, F_BFS_adjacency])) & (!((_C = h[_this, F_BFS_adjacency])) & (!((_V = _this)) & (!((_N = _this)) & (!((_C = _this)) & ((\forall i; (((0 <= i) & (i < a_size)) -> (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _V)) & (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _N)) & !((h[h[_this, F_BFS_adjacency], idxRef(i)] = _C)))))) & ((arrlen(_V) = a_size) & ((arrlen(_N) = a_size) & (arrlen(_C) = a_size))))))))))))) & (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & ((a_v = _v) & ((0 <= _v) & (_v < a_size)))))), 0 ; "marking stone"
 sourceline 72
  a_w := 0
 loop1:
 sourceline 74
 sourceline 73
  goto body1, after2
 body1:
  assume (\exists i; (((a_w <= i) & (i < a_size)) & seqGet(a_succ(a_v), i))); "assume condition "
 sourceline 76
  a_t := a_w
 sourceline 77
  havoc a_w
  assume (((a_t <= a_w) & (a_w < a_size)) & (seqGet(a_succ(a_v), a_w) & (\forall j; (((a_t <= j) & (j < a_w)) -> !(seqGet(a_succ(a_v), j))))))
 sourceline 80
  skip MARK, 3, (((a_V = boolArrAsSeq(h, _V)) & ((a_C = boolArrAsSeq(h, _C)) & ((a_N = boolArrAsSeq(h, _N)) & ((a_d = _d) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size]))))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & (!((_V = h[_this, F_BFS_adjacency])) & (!((_N = h[_this, F_BFS_adjacency])) & (!((_C = h[_this, F_BFS_adjacency])) & (!((_V = _this)) & (!((_N = _this)) & (!((_C = _this)) & ((\forall i; (((0 <= i) & (i < a_size)) -> (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _V)) & (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _N)) & !((h[h[_this, F_BFS_adjacency], idxRef(i)] = _C)))))) & ((arrlen(_V) = a_size) & ((arrlen(_N) = a_size) & (arrlen(_C) = a_size))))))))))))) & (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & ((a_v = _v) & (((0 <= _v) & (_v < a_size)) & ((a_w = _w) & ((0 <= _w) & (_w < a_size)))))))), 0 ; "marking stone"
 sourceline 82
  goto then1, else1
 then1:
  assume !(seqGet(a_V, a_w)); "then"
 sourceline 84
  a_V := seqUpdate(a_V, a_w, true)
 sourceline 85
  a_N := seqUpdate(a_N, a_w, true)
  goto after3
 else1:
 sourceline 86
  assume $not(!(seqGet(a_V, a_w))); "else"
 after3:
 sourceline 88
  skip MARK, 4, (((a_V = boolArrAsSeq(h, _V)) & ((a_C = boolArrAsSeq(h, _C)) & ((a_N = boolArrAsSeq(h, _N)) & ((a_d = _d) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size]))))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & (!((_V = h[_this, F_BFS_adjacency])) & (!((_N = h[_this, F_BFS_adjacency])) & (!((_C = h[_this, F_BFS_adjacency])) & (!((_V = _this)) & (!((_N = _this)) & (!((_C = _this)) & ((\forall i; (((0 <= i) & (i < a_size)) -> (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _V)) & (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _N)) & !((h[h[_this, F_BFS_adjacency], idxRef(i)] = _C)))))) & ((arrlen(_V) = a_size) & ((arrlen(_N) = a_size) & (arrlen(_C) = a_size))))))))))))) & (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & ((a_v = _v) & (((0 <= _v) & (_v < a_size)) & ((a_w = _w) & ((0 <= _w) & (_w < a_size)))))))), 0 ; "marking stone"
 sourceline 89
  a_w := (a_w + 1)
  goto loop1
 sourceline 73
 after2:
  assume $not((\exists i; (((a_w <= i) & (i < a_size)) & seqGet(a_succ(a_v), i))))
 sourceline 92
  skip MARK, 5, (((a_V = boolArrAsSeq(h, _V)) & ((a_C = boolArrAsSeq(h, _C)) & ((a_N = boolArrAsSeq(h, _N)) & ((a_d = _d) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size]))))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & (!((_V = h[_this, F_BFS_adjacency])) & (!((_N = h[_this, F_BFS_adjacency])) & (!((_C = h[_this, F_BFS_adjacency])) & (!((_V = _this)) & (!((_N = _this)) & (!((_C = _this)) & ((\forall i; (((0 <= i) & (i < a_size)) -> (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _V)) & (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _N)) & !((h[h[_this, F_BFS_adjacency], idxRef(i)] = _C)))))) & ((arrlen(_V) = a_size) & ((arrlen(_N) = a_size) & (arrlen(_C) = a_size))))))))))))) & (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & ((a_v = _v) & ((0 <= _v) & (_v < a_size)))))), 0 ; "marking stone"
 sourceline 94
  goto then2, else2
 then2:
  assume !((\exists i; (((0 <= i) & (i < a_size)) & seqGet(a_C, i)))); "then"
 sourceline 96
  a_C := a_N
 sourceline 97
  a_N := (\seqDef i; 0; a_size; false)
 sourceline 98
  a_d := (a_d + 1)
  goto after4
 else2:
 sourceline 99
  assume $not(!((\exists i; (((0 <= i) & (i < a_size)) & seqGet(a_C, i))))); "else"
 after4:
 sourceline 101
  skip MARK, 6, (((a_V = boolArrAsSeq(h, _V)) & ((a_C = boolArrAsSeq(h, _C)) & ((a_N = boolArrAsSeq(h, _N)) & ((a_d = _d) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size]))))))) & ((!((_V = _N)) & (!((_N = _C)) & (!((_V = _C)) & (!((_V = h[_this, F_BFS_adjacency])) & (!((_N = h[_this, F_BFS_adjacency])) & (!((_C = h[_this, F_BFS_adjacency])) & (!((_V = _this)) & (!((_N = _this)) & (!((_C = _this)) & ((\forall i; (((0 <= i) & (i < a_size)) -> (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _V)) & (!((h[h[_this, F_BFS_adjacency], idxRef(i)] = _N)) & !((h[h[_this, F_BFS_adjacency], idxRef(i)] = _C)))))) & ((arrlen(_V) = a_size) & ((arrlen(_N) = a_size) & (arrlen(_C) = a_size))))))))))))) & (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & ((a_v = _v) & ((0 <= _v) & (_v < a_size)))))), 0 ; "marking stone"
  goto loop0
 sourceline 58
 after0:
  assume $not((\exists k; (((0 <= k) & (k < a_size)) & seqGet(a_C, k))))
 sourceline 105
  a_d := -(1)
 endOfProgram: 


problem (((\forall v; (((0 <= v) & (v < a_size)) -> (a_succ(v) = boolArrAsSeq(h, h[h[_this, F_BFS_adjacency], idxRef(v)])))) & (\forall i; (((0 <= i) & (i < a_size)) -> (arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) = a_size)))) & ((a_src = _src) & ((a_dest = _dest) & (a_size = h[_this, F_BFS_size])))) |- INITIAL_VAR(0) -> [0; Java][<0;bfs_array>]((a_d = resInt))
