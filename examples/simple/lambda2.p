include
  "$int.p"
  "$fol.p"
  "$map.p"

function
 map(int, int) m assignable

problem
  m = $store(m, 2, 3) -> $load((\lambda x; $load(m, x) + x), 2) = 5
