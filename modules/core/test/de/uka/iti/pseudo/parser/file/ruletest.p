include
        "$base.p"
        "$int.p"
        
function
	int t
	int ass assignable

axiom axiom1
  true

axiom axiom2
  false -> false
  tags tautology "yes"
        
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
     freshVar  %x, 1 
   samegoal
     replace  true 
     
rule test_mod
     find  [ 1;P ]%b
   samegoal
     replace  cond(%b, [ 2;P ]%b, [[ 3;P ]]%b)

rule test_schema_mod
   find  [ %a ] %b
   replace  %a -> %b

program P source "somefilename"
  label: assume t > 0
    assert t < 0
    havoc ass
    sourceline 5
  label2:
    ass := t + 1
    goto label, label2 
    