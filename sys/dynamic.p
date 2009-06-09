include
   "$fol.p"

rule skip
  find { [skip]%t }
  samegoal replace { %t }

rule split_program
  find { [&a ; &b]%t }
  samegoal replace { [&a]([&b]%t) }

rule assignment
  find {[ %x := %v ]%t }
  where
    programFree {%t}
  samegoal replace { $$subst(%x, %v, %t) }

rule if_then_else
  find { [ if %c then &a else &b end]%t }
  samegoal replace { cond(%c, [&a]%t, [&b]%t) }

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

