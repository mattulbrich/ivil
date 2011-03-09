# Automatically created on Wed Mar 09 19:07:02 CET 2011
include "dij.algo.p"
function node start assignable
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
 sourceline 17
  assume ( \forall x ; ( \forall y ; weight ( x , y ) >= 0 ) ) 
 sourceline 19
  dom_distance := emptyset 
 sourceline 20
  distance := write(distance, start , 0 )
 sourceline 21
  dom_distance := singleton ( start ) 
 sourceline 23
  visited := emptyset 
 sourceline 26
 loop0:
  skip_loopinv visited <: dom_distance & read(distance, start ) = 0 & start :: dom_distance & ( \forall x ; x :: dom_distance -> read(distance, x ) >= 0 ) & ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: visited & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) & ( \forall a ; ( \forall b ; a :: visited & b :: dom_distance & pair ( a , b ) :: dom_weight -> read(distance, b ) <= read(distance, a ) + weight ( a , b ) ) ) & ( \forall a ; ( \forall b ; a :: visited & b :: dom_distance \ visited -> read(distance, a ) <= read(distance, b ) ) ) 
  goto body0, after0
 body0:
  assume ! emptyset = ( dom_distance \ visited ) ; "assume condition "
 sourceline 53
  assert (\exists n; ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance \ visited -> read(distance, n ) <= read(distance, m ) ) ) ; "assert before choose"
  havoc n
  assume ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance \ visited -> read(distance, n ) <= read(distance, m ) ) 
 sourceline 59
  visited := visited \/ singleton ( n ) 
 sourceline 62
  assert visited <: dom_distance 
  assume visited <: dom_distance 
 sourceline 67
  old_distance := distance 
 sourceline 68
  old_dom_distance := dom_distance 
 sourceline 69
  nbors0 := ( \set k ; pair ( n , k ) :: dom_weight & ! k :: visited ) 
 sourceline 70
  nbors := nbors0 
 sourceline 72
 loop1:
  skip_loopinv nbors <: nbors0 & ( \forall r ; r :: visited -> read(distance, r ) = read(old_distance, r ) ) & old_dom_distance <: dom_distance & ( \forall s ; s :: old_dom_distance -> read(distance, s ) <= read(old_distance, s ) ) & read(distance, start ) = 0 & start :: dom_distance & ( \forall x ; x :: dom_distance -> read(distance, x ) >= 0 ) & ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: visited & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) & ( \forall b ; b :: dom_distance \ nbors & pair ( n , b ) :: dom_weight -> read(distance, b ) <= read(distance, n ) + weight ( n , b ) ) & ( \forall a ; ( \forall b ; a :: visited & b :: dom_distance \ visited -> read(distance, a ) <= read(distance, b ) ) ) 
  goto body1, after1
 body1:
  assume ( \exists t ; t :: nbors ) ; "assume condition "
 sourceline 109
  assert (\exists o; o :: nbors ) ; "assert before choose"
  havoc o
  assume o :: nbors 
 sourceline 110
  nbors := nbors \ singleton ( o ) 
 sourceline 113
  assert pair ( n , o ) :: dom_weight 
 sourceline 116
  d := read ( distance , n ) + weight ( n , o ) 
 sourceline 118
  goto then0, else0
 then0:
  assume ! o :: dom_distance | d < read(distance, o ) ; "then"
 sourceline 120
  dom_distance := dom_distance \/ singleton ( o ) 
 sourceline 121
  distance := write(distance, o , d )
  goto after2
 else0:
  assume $not(! o :: dom_distance | d < read(distance, o ) ); "else"
 after2:
  goto loop1
 after1:
  assume $not(( \exists t ; t :: nbors ) )
  goto loop0
 after0:
  assume $not(! emptyset = ( dom_distance \ visited ) )
 sourceline 127
  assert visited = dom_distance 
 sourceline 128
  assert ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: dom_distance & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) 
 sourceline 131
  assert ( \forall a ; a :: dom_distance -> ( \forall b ; b :: dom_distance & pair ( a , b ) :: dom_weight -> read(distance, b ) <= read(distance, a ) + weight ( a , b ) ) ) 

