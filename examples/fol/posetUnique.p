
#
# Minimum in a partial ordered set is unique
#


include "$base.p" "$decproc.p" "$fol.p"

sort S

function
  bool $rel(S,S) infix <: 90

axiom trans
  (\forall x; (\forall y; (\forall z;
    x <: y & y <: z -> x <: z)))

axiom refl
  (\forall x; x <: x)

axiom antisymm
  (\forall x; (\forall y; x <: y & y <: x -> x=y))

rule refl
  find %x <: %x
  replace true
  tags rewrite "fol simp"

function
  S a
  S b
  S c

problem
   (\forall x; a <: x) ,
   (\forall x; b <: x) 
|- a = b