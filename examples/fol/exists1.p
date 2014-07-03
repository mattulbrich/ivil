(*
 * This problem emerged when doing the verification for
 * arrayMin.p: Z3 was unable to find the witness.
 *)

include "$fol.p"
include "$int.p"
include "$decproc.p"

axiom rule left_all_instantiate
  find (\forall %x as %'x; %y as %'x ~~> %b) |-
  add $$subst(%x, %y, %b) |-
  tags
    rewrite "fol add"

axiom rule exists_right_instantiate
  find |- (\exists %x as %'x; %y as %'x ~~> %b)
  add |- $$subst(%x, %y, %b)
  tags
    rewrite "fol add"

function int array(int)

lemma problem
  (\exists i; 0 ~~> 0<=i & i<1 & array(i) = array(0))
