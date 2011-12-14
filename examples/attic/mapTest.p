include
  "$int.p"
  "$symbex.p"
  "$map.p"


function
  map(int, int) data assignable
  map(int, bool) test assignable
  
program P
  data := $store(data, 1, 0)
  
  assert (\T_ex 'a; 0 = $load(data, 1) as 'a)

  test := $store(test,0,true)
  test := $store(test,1,false)

  assert !$load(test,0) = $load(test,1)

problem
 [0;P]true
