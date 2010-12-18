# this example test two dimensional maps

include
  "$int.p"
  "$symbex.p"
  "mapN.p"


function
  map(int, int, bool) data assignable
  
program P
  data := store(data, 0,0, true)
  
  assert load(data, 0,0)

  data := store(data,0,1,true)
  data := store(data,1,1,false)

  assert !load(data,0,0) = load(data,1,1) & load(data, 0,1)

problem
 [0;P]
