(*
 A small demonstration of how to use lambdas in arbitrary map types. As you can
 see, it is not very comfortable.
*)

include "$ivil.p" "$map.p"

sort 
  m as {'a}['a]'a

function
  m x assignable
  bool $convert(m, map('a, 'a))
  
# note: in order to use this rule, you first have to instantiate the type quantifier
rule load_convert_m
  find $load_m(%m, %y)
  assume $convert(%m, (\lambda %x;%e)) |-
  replace $$subst(%x, %y, %e)
  tags rewrite "concrete"

program P
  assume (\T_all 'a; $convert(x, (\lambda i as 'a; i)))
  assert x[0] = 0
  assert x[2:=4][2] = 4
  assert x[2:=4][0] = 0

  x[5] := 7
  assert x[5] = x[7]
