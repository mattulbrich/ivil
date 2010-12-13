#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains definitions and rules for handling the
 * integer type:
 *  - arithmetic operations
 *  - comparison predicates
 *  - the sum binder
 *)

include 
  "$base.p"

plugin
  # int literal evaluation
  metaFunction : "de.uka.iti.pseudo.rule.meta.IntEvalMetaFunction"

  # check whether a term is a number literal
  whereCondition : "de.uka.iti.pseudo.rule.where.IntLiteral"

function  # infixes
        int $pow(int, int)      infix ^  80
        int $div(int, int)      infix /  70 
        int $mod(int, int)      
        int $mult(int, int)     infix *  70 
        int $plus(int, int)     infix +  60 
        int $minus(int, int)    infix -  60

        int $shl(int, int)      infix << 55
        int $shr(int, int)      infix >> 55
        int $ushr(int, int)     infix >>> 55
       
        bool $lt(int, int)      infix <  50
        bool $gt(int, int)      infix >  50
        bool $gte(int, int)     infix >= 50
        bool $lte(int, int)     infix <= 50
        
function  # prefixes
        int $neg(int)           prefix - 60

binder
        int (\sum int; int; int; int)

(*
 * Rules concerning + and -
 *)
  
rule plus_zero
  find %a + 0
  replace %a 
  tags rewrite "fol simp"

rule zero_plus
  find 0 + %a
  replace %a 
  tags rewrite "fol simp"

rule plus_assoc1
  find %a + (%b + %c)
  replace %a + %b + %c

rule plus_assoc2
  find %a + %b + %c
  replace %a + (%b + %c)

rule plus_comm
  find %a + %b
  replace %b + %a

rule minus_is_plus
  find %a - %b
  replace %a + (-%b)

(*
 * Rules concerning *
 *)
rule times_one
  find 1 * %a
  replace %a


(*
 * Rules concerning /
 *)
rule divide_by_one
  find %a / 1
  replace %a

rule divide_by_self
  assume |- %a = 0
  find %a / %a
  replace 1

(*
 * Rules concerning /
 *)
rule divide_by_one
  find %a / 1
  replace %a

rule divide_by_self
  assume |- %a = 0
  find %a / %a
  replace 1

(*
 * Rules concerning >, <, >=, <=
 *)

rule gte_to_gt
  find %a >= %b
  replace %a > %b | %a = %b
  tags verbosity "8"

rule gte_to_nlt
  find %a >= %b
  replace !%a < %b
  tags verbosity "8"

rule lte_to_ngt
  find %a <= %b
  replace !%a > %b
  tags verbosity "8"

rule lte_to_lt
  find %a <= %b
  replace %a < %b | %a = %b
  tags verbosity "8"

rule gt_to_lt
  find %a > %b
  replace %b < %a
  tags verbosity "8"

rule gt_minus_one
  find %a + (-1) >= %b
  replace %a > %b
  tags verbosity "8"

(*
 * Handling expressions with only literals
 *)
# TODO: later have rules for the possible operators
# themselves so that it matches less often.

rule resolve_int_literals
  find %t
  where
    canEval($$intEval(%t))
  samegoal
    replace $$intEval(%t)
  tags rewrite "fol simp"
       verbosity "8"
