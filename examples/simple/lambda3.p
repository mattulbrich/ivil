include
  "$symbex.p"
  "$int.p"
  "$fol.p"
  "$map.p"

function
 map(int, int) m assignable
 
program P
  m := (\lambda x; x/2)
  assert $load(m, 3) = $load(m, 2)

lemma problem
  [0;P]
  
  
