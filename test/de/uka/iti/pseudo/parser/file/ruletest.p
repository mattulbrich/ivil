include
        "$base.p"
        
function
	int t
        
rule rule_Test1
        find { 1 }
        copygoal
                replace { 2 }

rule forall_right
   find |- { (\forall x as int; x > 0) }
   copygoal
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
   copygoal
     replace { 2 }