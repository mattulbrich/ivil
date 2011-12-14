#
# Simple test case for the built in map type
#

include "$ivil.p"

sort 
  m as {'a}['a]'a

function
  m x assignable

program P
  x := x[0 := 1]
  assert x[0] = 1
  assert x[2:=4][2] = 4
  assert x[2:=4][0] = 1

  x[5] := 7
  assert x[5] = 7
