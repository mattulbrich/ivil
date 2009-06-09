include
   "$fol.p"

rule skip
  find { [skip]%t }
  samegoal replace { %t }

rule split_program
  find { [&a ; &b]%t }
  samegoal replace { [&a]([&b]%t) }

# rule assignment
#   find { [ %x := %v ]%t }
#   samegoal replace { $$subst(%x, %v, %t) }

rule if_then_else
  find { [ if %c then &a else &b end]%t }
  samegoal replace { cond(%c, [&a]%t, [&b]%t) }

rule if_then
  find { [ if %c then &a end]%t }
  samegoal replace { cond(%c, [&a]%t, %t) }


#rule andRight
#        find  |- {%b & %c}
#        replace {%b}
#        replace {%c}
