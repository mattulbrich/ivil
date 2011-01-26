include
  "$int.p"
  "$fol.p"
  "$map.p"

problem
  load(load((\lambda x; (\lambda y; x + y)), 2), 3) = 5 & load(load((\lambda x; (\lambda y; x ^ y)), 2), 3) = 8