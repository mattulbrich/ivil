# Automatically created on Thu May 26 18:25:40 CEST 2011
include "array.p"
include "$symbex.p"
include "$decproc.p"
function map(int,int) h assignable
function int last assignable
function int i assignable
function int min assignable
function map(int,int) h0 assignable
function map(int,int) perm assignable
function int result assignable

program PrioQueueTake source "prioq.algo"
 sourceline 26
  assume len ( h ) > 0 ; "pre cond" 
 sourceline 27
  assume ( \forall l ; 1 <= l & l < len ( h ) -> read(h, l ) >= read(h, ( l - 1 ) / 2 ) ) ; "invariant" 
 sourceline 29
  h0 := h 
 sourceline 31
  assert 0 <= 0 & 0 < len ( h ) ; "range check h[0]" 
 sourceline 32
  result := read(h, 0 ) 
 sourceline 34
  assert 0 <= len ( h ) - 1 & len ( h ) - 1 < len ( h ) ; "range check h[len(h) - 1]" 
 sourceline 35
  last := read(h, len ( h ) - 1 ) 
 sourceline 37
  assert 0 <= len ( h ) - 1 & len ( h ) - 1 < len ( h ) ; "range check removeLast" 
 sourceline 38
  h := removeNo ( h , len ( h ) - 1 ) 
 sourceline 40
  goto then0, else0
 then0:
  assume len ( h ) > 0 ; "then"
 sourceline 43
  assert 0 <= 0 & 0 < len ( h ) ; "range check h[0]" 
 sourceline 44
  h := write(h, 0 , last )
 sourceline 46
  perm := insert ( idPerm ( len ( h ) - 1 ) , 0 , len ( h ) - 1 ) 
 sourceline 47
  assert isPermN ( perm ) 
  assume isPermN ( perm ) 
 sourceline 49
  i := 0 
 sourceline 50
 loop0:
  skip_loopinv len ( h ) = len ( h0 ) - 1 & i >= 0 & ( \forall j ; 1 <= j & j < len ( h ) -> cond ( i = ( j - 1 ) / 2 , ! i = 0 -> read(h, j ) >= read(h, ( i - 1 ) / 2 ) , read(h, j ) >= read(h, ( j - 1 ) / 2 ) ) ) & isPerm ( h , removeNo ( h0 , 0 ) ) 
  goto body0, after1
 body0:
  assume i < len ( h ) / 2 ; "assume condition "
 sourceline 63
  goto then1, else1
 then1:
  assume 2 * i + 2 = len ( h ) ; "then"
 sourceline 65
  min := 2 * i + 1 
  goto after2
 else1:
  assume $not(2 * i + 2 = len ( h ) ); "else"
 sourceline 67
  assert 0 <= 2 * i + 1 & 2 * i + 1 < len ( h ) ; "rangecheck h[2*i + 1]" 
 sourceline 68
  assert 0 <= 2 * i + 2 & 2 * i + 2 < len ( h ) ; "rangecheck h[2*i + 2]" 
 sourceline 69
  goto then2, else2
 then2:
  assume read(h, 2 * i + 1 ) > read(h, 2 * i + 2 ) ; "then"
 sourceline 71
  min := 2 * i + 2 
  goto after3
 else2:
  assume $not(read(h, 2 * i + 1 ) > read(h, 2 * i + 2 ) ); "else"
 sourceline 73
  min := 2 * i + 1 
 after3:
 after2:
 sourceline 77
  assert 0 <= i & i < len ( h ) ; "rangecheck h[i]" 
 sourceline 78
  assert 0 <= min & min < len ( h ) ; "rangecheck h[min]" 
 sourceline 79
  goto then3, else3
 then3:
  assume read(h, i ) > read(h, min ) ; "then"
 sourceline 81
  h := swap ( h , i , min ) 
 sourceline 82
  i := min 
  goto after4
 else3:
  assume $not(read(h, i ) > read(h, min ) ); "else"
 sourceline 84
  i := len ( h ) 
 after4:
  goto loop0
 after1:
  assume $not(i < len ( h ) / 2 )
  goto after0
 else0:
  assume $not(len ( h ) > 0 ); "else"
 after0:
 sourceline 90
  assert len ( h ) = len ( h0 ) - 1 
 sourceline 91
  assert result = read(h0, 0 ) 
 sourceline 92
  assert ( \forall k ; 1 <= k & k < len ( h ) -> read(h, k ) >= read(h, ( k - 1 ) / 2 ) ) ; "invariant" 
 sourceline 93
  assert isPerm ( h , removeNo ( h0 , 0 ) ) 

