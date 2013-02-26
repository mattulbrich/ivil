include 
  "$int.p"
  "$decproc.p"
  "$set.p"
  "$symbex.p"

function
  # this makes every type countable!
  int k('a) unique
  set(int) S assignable

problem 
  false

(*

Do a cut:
{ S := (\set n; (\exists s; k(s)=n & !n :: s)) }
  (k(S) :: S)


*)