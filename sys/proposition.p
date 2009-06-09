include
   "$base.p"

rule and_right
  find |- { %a & %b }
  samegoal replace { %a }
  samegoal replace { %b }

rule and_left
  find { %a & %b } |-
  samegoal replace { %a }
           add { %b } |-

rule or_right
  find |- { %a | %b }
  samegoal replace { %a }
           add |- { %b }

rule or_left
  find { %a | %b } |-
  samegoal replace { %a }
  samegoal replace { %b }

rule impl_right
  find |- { %a -> %b }
  samegoal replace { %b }
           add { %a } |-

rule impl_left
  find { %a -> %b } |-
  samegoal "show %a"
    remove
    add |- { %a }
  samegoal "use %b"
    replace { %b }

rule not_right
  find |- { !%b }
  samegoal remove
           add { %b } |-

rule not_left
  find { !%b } |-
  samegoal remove
           add |- { %b }

rule and_true_l
  find { true & %a }
  samegoal replace { %a }

rule and_false_l
  find { false & %a }
  samegoal replace { false }

rule and_true_r
  find { %a & true }
  samegoal replace { %a }

rule and_true_r
  find { %a & false }
  samegoal replace { false }

rule close_same
  find |- { %b }
  assume { %b } |-
  closegoal

rule close_true_right
  find |- { true }
  closegoal

rule close_false_left
  find { false } |-
  closegoal

rule replace_known_left
  find { %b }
  assume { %b } |-
  samegoal replace { true }

rule replace_known_right
  find { %b }
  assume |- { %b }
  samegoal replace { false }

rule cut
  find { %c }
  where
    interact { %inst as bool }
  samegoal add |- { %inst }
  samegoal add    { %inst } |-

rule cutOnThat
  find { %c }
  samegoal
    replace { true }
    add { %c } |-
  samegoal
    replace { false }
    add |- { %c }


#rule forAllRight
#        find  |- { (\forall %x; %b) }
#        replace { (\subst %x; c; %b) }

#rule andRight
#        find  |- {%b & %c}
#        replace {%b}
#        replace {%c}
