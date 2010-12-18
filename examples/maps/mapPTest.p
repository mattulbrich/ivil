# this example uses pairs to simaluate two dimensional maps

include
  "$int.p"
  "$symbex.p"
  "mapP.p"


function
  map(pair(int, int), bool) data assignable
  
program P
  data := store(data, pair(0,1), true)
  
  assert load(data, pair(0,1))

  data := store(data,pair(0,0),true)
  data := store(data,pair(1,1),false)

  assert !load(data,pair(0,0)) = load(data,pair(1,1))

problem
 [0;P]
