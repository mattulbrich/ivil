(*
  this example shows basic map functionality
 *)

include
  "$all.p"

sort
  heap as {'a}['a]'a

function
  heap x assignable

program P
  x := $store_heap(x, 0, 5)
  assume 0 = 0
  #assume !0 = 0
  assert $load_heap(x,0) = 5

  x := $store_heap(x, false, !$load_heap(x, true))
  
  x := $store_heap(x, 1, 1)

  assert (\forall b as bool; $load_heap(x,b) | $load_heap(x,!b))
