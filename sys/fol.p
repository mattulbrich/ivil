include
   "$proposition.p"

rule forall_right
  find  |- { (\forall %x as 'a; %b) }
  samegoal replace { $$subst(%x, $$skolem(%x), %b) }

rule exists_right
  find  |- { (\exists %x as 'a; %b) }
  where
    interact {%inst as 'a}
  samegoal replace { $$subst(%x, %inst, %b) }

rule forall_left
  find { (\forall %x as 'a; %b) } |-
  where
    interact {%inst as 'a}
  samegoal replace { $$subst(%x, %inst, %b) }

rule exists_left
  find  { (\exists %x as 'a; %b) } |-
  samegoal replace { $$subst(%x, $$skolem(%x), %b) }

rule equality
  find { %t = %t }
  samegoal replace { true }

rule cond_true
  find { cond(true, %a, %b) }
  samegoal replace { %a }

rule cond_false
  find { cond(false, %a, %b) }
  samegoal replace { %b }

#rule andRight
#        find  |- {%b & %c}
#        replace {%b}
#        replace {%c}
