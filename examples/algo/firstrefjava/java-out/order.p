#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2012 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich, Timm Felden
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains definitions, rules and axioms for handling of a total order.
 *)
 
function 
  bool $order_less('a, 'a)
  bool $order_greater('a, 'a)
  bool $order_greater_equal('a, 'a)
  bool $order_less_equal('a, 'a)
  
    
(*
 rules to remove greater from the sequent replacing it by less
*)

rule order_greater_to_less
find $order_greater(%a, %b)
replace $order_less(%b, %a)
tags rewrite "fol simp"

rule order_gte_to_lte
find $order_greater_equal(%a, %b)
replace $order_less_equal(%b, %a)
tags rewrite "fol simp"

(*
 rules to move orders to the left side of the sequent
*)
  
rule order_less_right
  find |- $order_less(%a, %b)
  samegoal remove
           add  $order_less_equal(%b, %a)  |-
  tags rewrite "fol simp"
  
rule order_less_equal_right
  find |- $order_less_equal(%a, %b)
  samegoal remove
           add  $order_less(%b, %a)  |-
  tags rewrite "fol simp"
  
(*
 rules for int orders
*)
rule order_less_to_lt
  find $order_less(%a, %b)
  replace %a < %b
  tags rewrite "fol simp"
  
rule order_less_equal_to_lte
  find $order_less_equal(%a, %b)
  replace %a <= %b
  tags rewrite "fol simp"
  
(*
 non confluent order rules
 *)
  
rule order_less_trans
  find $order_less(%a, %b) |-
  where not presentInSequent $order_less(%a, %c)
  assume $order_less(%b, %c) |-
  samegoal add $order_less(%a, %c) |-
  tags dragdrop "5"
 	   rewrite "split"
  
rule order_less_equal_split
  find $order_less_equal(%a, %b) |-
  replace %a = %b | $order_less(%a, %b)
  tags rewrite "split"

(*
 close rules
*)
rule order_less_close
  find $order_less(%a, %b) |-
  assume $order_less(%b, %a) |-
  closegoal
  tags rewrite "close"
       dragdrop "9"
       
rule order_less_equal_close
  find $order_less(%a, %b) |-
  assume $order_less_equal(%b, %a) |-
  closegoal
  tags rewrite "close"
       dragdrop "9"
