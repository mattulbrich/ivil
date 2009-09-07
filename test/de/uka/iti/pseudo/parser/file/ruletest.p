include
        "$base.p"
        "$int.p"
        
function
	int t
	int ass assignable
        
rule rule_Test1
        find 1 
        samegoal
                replace 2 

rule forall_left
   find (\forall x as 'a; %b)  |-
   where
      interact %inst as 'a
   samegoal
      add |-  %inst = %inst 

rule forall_right
   find |-  (\forall x as int; x = 0) 
   samegoal
      add |-  t > 0 


rule close_true
   find |- true
   closegoal

rule close_false
   find false |-
   closegoal
   
rule test_where
   find (\forall %x; %b) 
   where
     notFreeIn  %x, 1 
   samegoal
     replace  true 
     
rule test_mod
     find  [ 1;P ]
   samegoal
     replace  cond(%b, [ 2;P ], [[ 3;P ]])

rule test_schema_mod
   find  [ %a ]
   replace  %a -> %a

program P source "somefilename"
  label: assume t > 0
    assert t < 0
    havoc ass
    sourceline 5
  label2:
    ass := t + 1
    goto label, label2 
    