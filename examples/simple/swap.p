include
  "$symbex.p"
  "$int.p"
  "$fol.p"
  "$map.p"

function
 int x assignable
 int y assignable
 int x_ assignable
 int y_ assignable
 
program P
  assume x = x_ & y = y_
  x := y || y := x
  assert x = y_ & y = x_

problem
  [0;P]
  
  
