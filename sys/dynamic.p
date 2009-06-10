include
   "$fol.p"

rule skip
  find { [skip]%t }
  samegoal replace { %t }
  tags rewrite "symbex"

rule split_program
  find { [&a ; &b]%t }
  samegoal replace { [&a]([&b]%t) }

rule assignment
  find {[ %x := %v ]%t }
  where
    programFree {%t}
  samegoal replace { $$subst(%x, %v, %t) }
  tags rewrite "symbex"

rule if_then_else
  find { [ if %c then &a else &b end]%t }
  samegoal replace { cond(%c, [&a]%t, [&b]%t) }

rule if_then_else_split
  find |- { [ if %c then &a else &b end]%t }
  samegoal "then branch"
    replace { [&a]%t }
    add { %c } |-
  samegoal "else branch"
    replace { [&b]%t }
    add { !%c } |-

rule if_then
  find { [ if %c then &a end]%t }
  samegoal replace { cond(%c, [&a]%t, %t) }

rule while_inv_first
  find |- { [ while %c do &a end]%t }
  where
    interact { %inv }
  samegoal "{%inv} initially"
    replace { %inv }
  newgoal "body preserves {%inv}"
    add          |- { [&a]%inv }
    add { %inv } |-
    add { %c}    |-
  newgoal "use case"
    add { !%c }  |-
    add { %inv } |-
    add          |- { %t }

rule while_inv
  find |- { [&before][while %c do &a end]%t }
  where
    interact { %inv }
  samegoal "{%inv} initially"
    replace { [&before]%inv }
  newgoal "body preserves {%inv}"
    add          |- { [&a]%inv }
    add { %inv } |-
    add { %c}    |-
  newgoal "use case"
    add { !%c }  |-
    add { %inv } |-
    add          |- { %t }

rule while_given_inv
  find |- { [&before][while %c inv %inv do &a end]%t }
  samegoal "{%inv} initially"
    replace { [&before]%inv }
  newgoal "body preserves {%inv}"
    add          |- { [&a]%inv }
    add { %inv } |-
    add { %c}    |-
  newgoal "use case"
    add { !%c }  |-
    add { %inv } |-
    add          |- { %t }
  tags rewrite "symbex"