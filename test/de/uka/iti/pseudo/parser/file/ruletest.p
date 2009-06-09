include
        "$base.p"
        
function
	int t
        
rule rule_Test1
        find { 1 }
        samegoal
                replace { 2 }

rule forall_right
   find |- { (\forall x as int; x > 0) }
   samegoal
      add |- { t > 0 }


rule close_true
   find |- { true }
   closegoal

rule close_false
   find {false} |-
   closegoal
   
rule test_where
   find { 1 }
   where
     notFreeIn { 0 } { 1 }
   samegoal
     replace { 2 }
     
rule test_schema_mod
   find { [ &a ; if %c then &b else &c end ] %phi }
   samegoal
     replace { [ &a ] ((%c -> [&b]%phi) & (!%c -> [&c]%phi)) }