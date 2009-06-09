include
  "$dynamic.p"
  "$int.p"


function
  int val assignable

  bool b1
  bool b2
  bool p(int)

problem
#{ (\exists x; x = arb) }
#{ (\exists x; p(x)) -> !(\forall x; !p(x)) }
{ [if b1 then val:=7 else val:=9 end]val>0 }
