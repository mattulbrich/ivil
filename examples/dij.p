# Automatically created on Fri Feb 18 14:54:37 CET 2011
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
  skip_loopinv visited <: dom_distance & read(distance, start ) = 0 & start :: dom_distance & ( \forall x ; x :: dom_distance -> read(distance, x ) >= 0 ) & ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: dom_distance & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) 
  goto body0, after0
 body0:
  assume ! emptyset = ( dom_distance \ visited ) ; "assume condition "
 sourceline 37
  assert (\exists n; ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance \ visited -> read ( distance , n ) <= read ( distance , m ) ) ) ; "assert before choose"
  havoc n
  assume ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance \ visited -> read ( distance , n ) <= read ( distance , m ) ) 
 sourceline 42
  visited := visited \/ singleton ( n ) 
 sourceline 45
  assert visited <: dom_distance 
  assume visited <: dom_distance 
 sourceline 50
  old_distance := distance 
 sourceline 51
  old_dom_distance := dom_distance 
 sourceline 52
  S := singleton ( n ) <| dom_weight |> ^ visited 
 sourceline 54
 loop1:
  skip_loopinv S <: singleton ( n ) <| dom_weight |> ^ visited & visited <: dom_distance & dom_distance <: old_dom_distance & read(distance, start ) = 0 & start :: dom_distance & ( \forall y ; y :: dom_distance \ singleton ( start ) -> ( \exists x ; x :: dom_distance & pair ( x , y ) :: dom_weight & read(distance, y ) = read(distance, x ) + weight ( x , y ) ) ) 
  goto body1, after1
 body1:
  assume ! emptyset = S ; "assume condition "
 sourceline 65
  assert (\exists u; (\exists o; pair ( o , u ) :: S )) ; "assert before choose"
  havoc u
  havoc o
  assume pair ( o , u ) :: S 
 sourceline 66
  S := S \ singleton ( pair ( o , u ) ) 
 sourceline 69
  assert n = o 
 sourceline 72
  assert pair ( o , u ) :: dom_weight 
 sourceline 73
  d := read ( distance , n ) + weight ( o , u ) 
 sourceline 75
  goto then0, else0
 then0:
  assume d < read ( distance , u ) ; "then"
 sourceline 77
  dom_distance := dom_distance \/ singleton ( u ) 
 sourceline 78
  distance := write(distance, u , d )
  goto after2
 else0:
  assume $not(d < read ( distance , u ) ); "else"
 after2:
  goto loop1
 after1:
  assume $not(! emptyset = S )
  goto loop0
 after0:
  assume $not(! emptyset = ( dom_distance \ visited ) )

