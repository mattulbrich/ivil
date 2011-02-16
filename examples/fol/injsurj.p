include "$fol.p" "$decproc.p"

sort S

function 
  S a(S)
  S b(S)

problem

(* a surj *)
(\forall s; (\exists t; a(t) = s))
&
(* inverse *)
(\forall s; b(a(s)) = s)
->
(* b inj *)
(\forall s; (\forall t; b(s)=b(t) -> s=t))