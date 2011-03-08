# Automatically created on Tue Mar 08 17:12:08 CET 2011
include "dij.algo.p"
function node start assignable
function set(node) old_dom_distance assignable
function set(node) dom_distance assignable
function map(node,int) old_distance assignable
function map(node,int) distance assignable
function node n assignable
function node o assignable
function node u assignable
function prod(node,node) s assignable
function int d assignable
function set(node) visited assignable
function set(prod(node,node)) S assignable

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
  skip_loopinv visited <: dom_distance & read(distance, start ) = 0 & start :: dom_distance & ( \forall x ; x :: dom_distance -> read(distance, x ) >= 0 ) & ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: visited & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) 
  goto body0, after0
 body0:
  assume ! emptyset = ( dom_distance \ visited ) ; "assume condition "
 sourceline 48
  assert (\exists n; ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance \ visited -> read ( distance , n ) <= read ( distance , m ) ) ) ; "assert before choose"
  havoc n
  assume ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance \ visited -> read ( distance , n ) <= read ( distance , m ) ) 
 sourceline 53
  visited := visited \/ singleton ( n ) 
 sourceline 56
  assert visited <: dom_distance 
  assume visited <: dom_distance 
 sourceline 61
  old_distance := distance 
 sourceline 62
  old_dom_distance := dom_distance 
 sourceline 63
  S := singleton ( n ) <| dom_weight |> ^ visited 
 sourceline 65
 loop1:
  skip_loopinv S <: singleton ( n ) <| dom_weight |> ^ visited & ( \forall r ; r :: visited -> read(distance, r ) = read(old_distance, r ) ) & old_dom_distance <: dom_distance & read(distance, start ) = 0 & start :: dom_distance & ( \forall x ; x :: dom_distance -> read(distance, x ) >= 0 ) & ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: visited & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) 
  goto body1, after1
 body1:
  assume ! emptyset = S ; "assume condition "
 sourceline 88
  assert (\exists u; (\exists o; pair ( o , u ) :: S )) ; "assert before choose"
  havoc u
  havoc o
  assume pair ( o , u ) :: S 
 sourceline 89
  S := S \ singleton ( pair ( o , u ) ) 
 sourceline 92
  assert n = o 
 sourceline 95
  assert pair ( o , u ) :: dom_weight 
 sourceline 96
  d := read ( distance , n ) + weight ( o , u ) 
 sourceline 98
  goto then0, else0
 then0:
  assume ! u :: dom_distance | d < read(distance, u ) ; "then"
 sourceline 100
  dom_distance := dom_distance \/ singleton ( u ) 
 sourceline 101
  distance := write(distance, u , d )
  goto after2
 else0:
  assume $not(! u :: dom_distance | d < read(distance, u ) ); "else"
 after2:
  goto loop1
 after1:
  assume $not(! emptyset = S )
  goto loop0
 after0:
  assume $not(! emptyset = ( dom_distance \ visited ) )
 sourceline 107
  assert visited = dom_distance 
 sourceline 108
  assert ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: dom_distance & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) 
 sourceline 111
  assert ( \forall a ; a :: dom_distance -> ( \forall b ; b :: dom_distance & pair ( a , b ) :: dom_weight -> read(distance, b ) <= read(distance, a ) + weight ( a , b ) ) ) 

