# Automatically created on Wed Feb 16 17:31:18 CET 2011
include "dij.algo.p"
function node start assignable
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

program Dij
 sourceline 18
  dom_distance := emptyset 
 sourceline 19
  distance := write(distance, start , 0 )
 sourceline 20
  dom_distance := singleton ( start ) 
 sourceline 22
  visited := emptyset 
 sourceline 25
 loop0:
  skip_loopinv visited <: dom_distance & ( \forall x ; x :: visited -> ( ( \forall y ; pair ( x , y ) :: dom_weight -> read ( distance , y ) <= read ( distance , x ) + weight ( x , y ) ) & ( \exists y ; pair ( x , y ) :: dom_weight & read ( distance , y ) = read ( distance , x ) + weight ( x , y ) ) ) ) 
  goto body0, after0
 body0:
  assume ! emptyset = ( dom_distance \ visited ) ; "assume condition "
 sourceline 33
  assert (\exists n; ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance -> read ( distance , n ) <= read ( distance , m ) ) ) ; "assert before choose"
  havoc n
  assume ! n :: visited & n :: dom_distance & ( \forall m ; m :: dom_distance -> read ( distance , n ) <= read ( distance , m ) ) 
 sourceline 38
  visited := visited \/ singleton ( n ) 
 sourceline 43
  old_distance := distance 
 sourceline 44
  S := singleton ( n ) <| dom_weight |> ^ visited 
 sourceline 46
 loop1:
  skip_loopinv S <: singleton ( n ) <| dom_weight |> ^ visited & ( \forall x ; read ( distance , x ) <= read ( old_distance , x ) ) 
  goto body1, after1
 body1:
  assume ! emptyset = S ; "assume condition "
 sourceline 51
  assert (\exists u; (\exists o; pair ( o , u ) = s )) ; "assert before choose"
  havoc o
  havoc u
  assume pair ( o , u ) = s 
 sourceline 54
  assert n = o 
 sourceline 57
  assert pair ( o , u ) :: dom_weight 
 sourceline 58
  d := read ( distance , n ) + weight ( o , u ) 
 sourceline 60
  goto then0, else0
 then0:
  assume d < read ( distance , u ) ; "then"
 sourceline 62
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

