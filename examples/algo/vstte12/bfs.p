# Automatically created on Wed Nov 30 11:34:39 CET 2011
include "bfs.algo.p"
function vertex src assignable
function vertex dest assignable
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
 sourceline 26
  assume finite ( fullset as set ( vertex ) ) 
 sourceline 27
  size := card ( fullset as set ( vertex ) ) 
 sourceline 29
  V := singleton(src) 
 sourceline 30
  C := singleton(src ) 
 sourceline 31
  N := emptyset 
 sourceline 32
  d := 0 
 sourceline 33
 loop0:
  skip_loopinv d >= 0 & ( \forall x ; x :: C -> connect ( src , x , d ) ) & ( \forall y ; y :: N -> connect ( src , y , d + 1 ) ) & N <: V , size - card ( ( V \ N ) \ C ) 
  goto body0, after0
 body0:
  assume ! ( C = emptyset ) ; "assume condition "
 sourceline 41
  assert (\exists v; v :: C ) ; "assert before choose"
  havoc v
  assume v :: C 
 sourceline 42
  C := C \ singleton(v ) 
 sourceline 43
  goto then0, else0
 then0:
  assume v = dest ; "then"
 sourceline 45
  end ; "Return Statement"
  goto after1
 else0:
  assume $not(v = dest ); "else"
 after1:
 sourceline 48
  tovisit := succ ( v ) 
 sourceline 49
  Vo := V 
 sourceline 50
  No := N 
 sourceline 51
 loop1:
  skip_loopinv tovisit <: succ ( v ) & Vo <: V & No <: N & ( \forall y ; y :: N -> connect ( src , y , d + 1 ) ) , card ( succ ( v ) ) - card ( tovisit ) 
  goto body1, after2
 body1:
  assume ! tovisit = emptyset ; "assume condition "
 sourceline 58
  assert (\exists w; w :: tovisit ) ; "assert before choose"
  havoc w
  assume w :: tovisit 
 sourceline 59
  tovisit := tovisit \ singleton(w ) 
 sourceline 61
  goto then1, else1
 then1:
  assume ! w :: V ; "then"
 sourceline 63
  V := V \/ singleton(w ) 
 sourceline 64
  N := N \/ singleton(w ) 
  goto after3
 else1:
  assume $not(! w :: V ); "else"
 after3:
  goto loop1
 after2:
  assume $not(! tovisit = emptyset )
 sourceline 68
  goto then2, else2
 then2:
  assume C = emptyset ; "then"
 sourceline 70
  C := N 
 sourceline 71
  N := emptyset 
 sourceline 72
  d := d + 1 
  goto after4
 else2:
  assume $not(C = emptyset ); "else"
 after4:
  goto loop0
 after0:
  assume $not(! ( C = emptyset ) )
 sourceline 76
  d := - 1 


problem 
 |- [[0;bfs]]((d >= - 1 ) & (d < 0 -> ( \forall m ; ! connect ( src , dest , m ) ) ) & (d >= 0 -> connect ( src , dest , d ) ))
