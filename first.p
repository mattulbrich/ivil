include "$proposition.p"

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

rule cut
  find |- {%something}
  samegoal
    add {%c} |-
  samegoal
    add |- {%c}

rule mod_sep
  find { [&prog;&prog2]%t }
  samegoal
    replace {[&prog2]([&prog2]%t) }

rule mod_skip
  find { [skip]%t }
  samegoal
    replace { %t }


problem
  { [skip ; skip]b1 & b2 -> b2 }
