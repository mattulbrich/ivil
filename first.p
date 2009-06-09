include "$fol.p"

function
  bool b1
  bool b2
  bool p(int)

problem
{ (\exists x; x = arb) }
  #{ (\exists x; p(x)) -> !(\forall x; !p(x)) }
