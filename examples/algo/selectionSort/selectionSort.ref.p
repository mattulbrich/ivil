# Automatically created on Thu Aug 23 21:07:59 CEST 2012
include "selectionSort.decl.p"
include "heapAbs.p"
include "SelSort.sort(int[]).p"
include "$seq.p"
  plugin prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.SeqPrettyPrinter"

function seq(int) a 
function int i assignable
function int j assignable
function int t assignable
function int n assignable
function seq(int) b assignable

program selectionSort source "selectionSort.algo"
 sourceline 40
  b := a
 sourceline 41
  i := 0
 sourceline 42
  n := seqLen(a)
 sourceline 44
  goto then0, else0
 then0:
  assume (n = 0); "then"
 sourceline 46
  goto endOfProgram ; "Return Statement"
  goto after0
 else0:
 sourceline 47
  assume $not((n = 0)); "else"
 after0:
 loop0:
 sourceline 57
 sourceline 49
  goto body0, after1
 body0:
  assume (i < (n - 1)); "assume condition "
 sourceline 59
  t := i
 sourceline 60
  j := (i + 1)
 loop1:
 sourceline 72
 sourceline 61
  goto body1, after2
 body1:
  assume (j < n); "assume condition "
 sourceline 74
  goto then1, else1
 then1:
  assume (seqGet(b, j) < seqGet(b, t)); "then"
 sourceline 76
  t := j
  goto after3
 else1:
 sourceline 77
  assume $not((seqGet(b, j) < seqGet(b, t))); "else"
 after3:
 sourceline 78
  skip MARK, 1, ((b = arrayAsIntSeq(h, _array)) & ((n = seqLen(b)) & ((i = _i) & ((j = _j) & ((t = _t) & ((((0 <= _i) & (_i < arrlen(_array))) & (((0 <= _j) & (_j <= arrlen(_array))) & ((0 <= _t) & (_t < arrlen(_array))))) & (j < n))))))), 42 ; "marking stone"
 sourceline 79
  j := (j + 1)
  goto loop1
 sourceline 61
 after2:
  assume $not((j < n))
 sourceline 82
  b := seqSwap(b, i, t)
 sourceline 83
  skip MARK, 2, ((b = arrayAsIntSeq(h, _array)) & ((n = seqLen(b)) & ((i = _i) & ((j = _j) & (((0 <= _i) & (_i < arrlen(_array))) & (((0 <= _j) & (_j <= arrlen(_array))) & ((0 <= _t) & (_t < arrlen(_array))))))))), 42 ; "marking stone"
 sourceline 84
  i := (i + 1)
  goto loop0
 sourceline 49
 after1:
  assume $not((i < (n - 1)))
 endOfProgram: 


problem (!((_array = null)) & (a = arrayAsIntSeq(h, _array))) |- [0; Java][<0;selectionSort>]((b = arrayAsIntSeq(h, _array)))
