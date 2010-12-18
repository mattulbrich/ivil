# this example uses pairs to simaluate two dimensional maps

include
  "$int.p"
  "$symbex.p"
  "mapC.p"


function
  map(int, map(int, bool)) data assignable
  
program P
  data := store(data, 0, store(load(data, 0), 1, true))
  
  assert load(load(data, 0), 1)

data := store(data, 0, store(load(data, 0), 0, true))
data := store(data, 1, store(load(data, 1), 1, false))

  assert !load(load(data,0),0) = load(load(data,1),1)

problem
 [0;P]
