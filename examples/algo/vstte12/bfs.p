# Automatically created on Tue Dec 06 20:48:56 CET 2011
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
function set(vertex) done assignable
function set(vertex) done0 assignable

program bfs source "bfs.algo"
 sourceline 27
  assume finite ( fullset as set ( vertex ) ) 
 sourceline 28
  size := card ( fullset as set ( vertex ) ) 
 sourceline 30
  V := singleton(src ) 
 sourceline 31
  C := singleton(src ) 
 sourceline 32
  N := emptyset 
 sourceline 33
  d := 0 
 sourceline 34
  done := emptyset 
 loop0:
 sourceline 37
  skip_loopinv d >= 0 & ( \forall x ; x :: C -> connect ( src , x , d ) ) & ( \forall y ; y :: N -> connect ( src , y , d + 1 ) ) & N <: V & done /\ C = emptyset , card ( ^ done ) 
 sourceline 35
  goto body0, after0
 body0:
  assume ! ( C = emptyset ) ; "assume condition "
 sourceline 44
  assert (\exists v; v :: C ) ; "assert before choose"
  havoc v
  assume v :: C 
 sourceline 45
  C := C \ singleton(v ) 
 sourceline 46
  done := done \/ singleton(v ) 
 sourceline 47
  goto then0, else0
 then0:
  assume v = dest ; "then"
 sourceline 49
  end ; "Return Statement"
  goto after1
 else0:
  assume $not(v = dest ); "else"
 sourceline 50
 after1:
 sourceline 52
  tovisit := succ ( v ) 
 sourceline 53
  Vo := V 
 sourceline 54
  No := N 
 loop1:
 sourceline 56
  skip_loopinv tovisit <: succ ( v ) & Vo <: V & No <: N & N <: V & ( \forall y ; y :: N -> connect ( src , y , d + 1 ) ) , card ( tovisit ) 
 sourceline 55
  goto body1, after2
 body1:
  assume ! tovisit = emptyset ; "assume condition "
 sourceline 63
  assert (\exists w; w :: tovisit ) ; "assert before choose"
  havoc w
  assume w :: tovisit 
 sourceline 64
  tovisit := tovisit \ singleton(w ) 
 sourceline 66
  goto then1, else1
 then1:
  assume ! w :: V ; "then"
 sourceline 68
  V := V \/ singleton(w ) 
 sourceline 69
  N := N \/ singleton(w ) 
  goto after3
 else1:
  assume $not(! w :: V ); "else"
 sourceline 70
 after3:
  goto loop1
 sourceline 55
 after2:
  assume $not(! tovisit = emptyset )
 sourceline 73
  goto then2, else2
 then2:
  assume C = emptyset ; "then"
 sourceline 75
  C := N 
 sourceline 76
  N := emptyset 
 sourceline 77
  d := d + 1 
  goto after4
 else2:
  assume $not(C = emptyset ); "else"
 sourceline 78
 after4:
  goto loop0
 sourceline 35
 after0:
  assume $not(! ( C = emptyset ) )
 sourceline 81
  d := - 1 


problem 
 |- [[0;bfs]]((d >= - 1 ) & (d < 0 -> ( \forall m ; ! connect ( src , dest , m ) ) ) & (d >= 0 -> connect ( src , dest , d ) ))
