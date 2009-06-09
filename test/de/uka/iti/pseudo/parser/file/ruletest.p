include
        "$base.p"
        "$int.p"
        
function
	int t
        
rule rule_Test1
        find { 1 }
        samegoal
                replace { 2 }

rule forall_left
   find { (\forall x as 'a; %b) } |-
   where
      interact { %inst as 'a}
   samegoal
      add |- { %inst = %inst }

rule forall_right
   find |- { (\forall x as int; x = 0) }
   samegoal
      add |- { t > 0 }


rule close_true
   find |- { true }
   closegoal

rule close_false
   find {false} |-
   closegoal
   
rule test_where
   find { (\forall %x; %b) }
   where
     notFreeIn { %x } { 1 }
   samegoal
     replace { true }
     
rule test_schema_mod
   find { [ &a ; if %c then &b else &c end ] %val }
   samegoal
     replace { [ &a ] cond(%c, [&b]%val, [&c]%val) }
