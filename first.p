include "$fol.p"

function
  bool b1
  bool b2
  bool p(int)

problem
  { (\forall x; p(x)) -> (\exists x; p(x)) }
