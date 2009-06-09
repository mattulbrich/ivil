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

rule not_right
  find |- { !%b }
  samegoal replace {false}
           add { %b } |-

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


#rule forAllRight
#        find  |- { (\forall %x; %b) }
#        replace { (\subst %x; c; %b) }

#rule andRight
#        find  |- {%b & %c}
#        replace {%b}
#        replace {%c}
