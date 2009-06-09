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
  where
    distinctAssumeAndFind
  samegoal replace { true }

rule replace_known_right
  find { %b }
  assume |- { %b }
  where
    distinctAssumeAndFind
  samegoal replace { false }

rule cut
  find { %c }
  where
    interact { %inst as bool }
  samegoal "Assume true for {%c}"
    add    { %inst } |-
  samegoal "Assume false for {%c}"
    add |- { %inst }

rule cutOnThat
  find { %c }
  samegoal "Assume true for {%c}"
    replace { true }
    add { %c } |-
  samegoal "Assume false for {%c}"
    replace { false }
    add |- { %c }

