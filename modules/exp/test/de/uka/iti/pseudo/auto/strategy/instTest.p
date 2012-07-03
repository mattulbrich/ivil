include "$int.p"
  "$fol.p"

problem exEqInst1:
  |- (\exists x; x=0)

problem exEqInst2:
  |- (\exists x; (true & x=0) & true)