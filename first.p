include "$base.p"

function
  bool b1
  bool b2

rule imp_right
  find |- {%a -> %b}
  samegoal
    replace {%b}
    add {%a} |-

rule imp2or
  find {%a -> %b}
  samegoal
    replace {!%a | %b}

rule and_left
  find {%a & %b} |-
  samegoal
    replace {%a}
    add {%b} |-

rule close_same
  assume |- {%a}
  find {%a} |-
  closegoal

problem
  { b1 & b2 -> b2 }
