include
  "$all.p"

sort
  heap as {'a}['a]'a
  struct as {'a}['a]['a]'a

function
  heap x assignable
  struct y assignable

program P
  x := x[0 := 5]
  #used for drag & drop instantiation
  assume 0 = 0
  assert x[0] = 5

  y := y[0 := y[0][0 := 0] ]

  assert y[0][0] = 0
