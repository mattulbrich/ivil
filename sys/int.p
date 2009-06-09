include 
  "$base.p"

function  # infixes
        int $pow(int, int)      infix ^  80
        int $div(int, int)      infix /  70 
        int $mult(int, int)     infix *  70 
        int $plus(int, int)     infix +  60 
        int $minus(int, int)    infix -  60 
       
        bool $lt(int, int)      infix <  50
        bool $gt(int, int)      infix >  50
        bool $gte(int, int)     infix >= 50
        
function  # prefixes
        int $neg(int)           prefix - 60

binder
        int (\sum int; int; int; int)



(*
 * Rules concerning + and -
 *)
  
rule plus_zero
  find { %a + 0 }
  replace { %a } 

rule zero_plus
  find { 0 + %a }
  replace { %a } 

rule plus_assoc1
  find { %a + (%b + %c) }
  replace { %a + %b + %c }

rule plus_assoc2
  find { %a + %b + %c }
  replace { %a + (%b + %c) }

rule plus_comm
  find { %a + %b }
  replace { %b + %a }

rule minus_is_plus
  find { %a - %b }
  replace { %a + (-%b) }

(*
 * Rules concerning * and /
 *)
rule times_one
  find { 1 * %a }
  replace { %a }


(*
 * Rules concerning >, <, >=, <=
 *)

rule gte_to_gt
  find { %a >= %b }
  replace { %a > %b | %a = %b }

rule gt_minus_one
  find { %a + (-1) >= %b }
  replace { %a > %b }

(*
 * Handling expressions with only literals
 *)

rule resolve_int_literals
  find { %t }
  where
    canEval { $$intEval(%t) }
  samegoal
    replace { $$intEval(%t) }
