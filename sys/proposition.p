#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains rules for propositional logic
 *)

include
   "$base.p"

(*
 *  closing
 *)

rule close_antecedent
  find |-  %b 
  assume  %b  |-
  closegoal
  tags rewrite "close"

rule close_succedent
  find  %b  |-
  assume |-  %b 
  closegoal

rule close_true_right
  find |-  true 
  closegoal
  tags rewrite "close"

rule close_false_left
  find  false  |-
  closegoal
  tags rewrite "close"

rule true_left
  find  true  |-
  remove
  tags rewrite "concrete"
       verbosity "8"

rule false_right
  find |-  false 
  remove
  tags rewrite "concrete"
       verbosity "8"


(*
 * application rules / cut
 *)

rule replace_known_left
  find  %b 
  assume  %b  |-
  where
    toplevel
  where
    distinctAssumeAndFind
  samegoal replace  true 

rule replace_known_right
  find  %b 
  assume |-  %b 
  where
    toplevel
  where
    distinctAssumeAndFind
  samegoal replace  false 

rule cut
  where
    interact  %inst as bool 
  samegoal "Assume true for {%inst}"
    add     %inst  |-
  samegoal "Assume false for {%inst}"
    add |-  %inst 

rule cutOnThat
  find  %c 
  where
    toplevel
  samegoal "Assume true for {%c}"
    replace  true 
    add  %c  |-
  samegoal "Assume false for {%c}"
    replace  false 
    add |-  %c 

(*
 * stuff with and
 *)

rule and_right
  find |-  %a & %b 
  samegoal "Conj1: {%a}"
    replace  %a 
  samegoal "Conj2: {%b}"
    replace  %b 
  tags rewrite "split"

rule and_left
  find  %a & %b  |-
  samegoal replace  %a 
           add  %b  |-
  tags rewrite "prop simp"
       verbosity "6"

rule and_true_l
  find  true & %a 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

rule and_false_l
  find  false & %a 
  replace  false 
  tags rewrite "concrete"
       verbosity "8"

rule and_true_r
  find  %a & true 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

rule and_false_r
  find  %a & false 
  replace  false 
  tags rewrite "concrete"
       verbosity "8"

rule and_idempotent
  find  %a & %a 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

(*
 *  stuff with or
 *)

rule or_right
  find |-  %a | %b 
  samegoal replace  %a 
           add |-  %b 
  tags rewrite "prop simp"
       verbosity "6"

rule or_left
  find  %a | %b  |-
  samegoal "Disj1: {%a}"
    replace  %a 
  samegoal "Disj1: {%b}"
    replace  %b 
  tags rewrite "split"

rule or_true_l
  find  true | %a 
  replace  true 
  tags rewrite "concrete"
       verbosity "8"

rule or_false_l
  find  false | %a 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

rule or_true_r
  find  %a | true 
  replace  true 
  tags rewrite "concrete"
       verbosity "8"

rule or_false_r
  find  %a | false 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

rule or_idempotent
  find  %a | %a 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

(*
 *  stuff with impl
 *)
rule impl_right
  find |-  %a -> %b 
  samegoal replace  %b 
           add  %a  |-
  tags rewrite "prop simp"
       verbosity "6"

rule impl_left
  find  %a -> %b  |-
  samegoal "show {%a}"
    remove
    add |-  %a 
  samegoal "use {%b}"
    replace  %b 

rule impl_false_l
  find  false -> %b 
  replace  true 
  tags rewrite "concrete"
       verbosity "8"

rule impl_false_r
  find  %b -> false 
  replace  !%b 
  tags rewrite "concrete"
       verbosity "8"

rule impl_true_l
  find  true -> %b 
  replace  %b 
  tags rewrite "concrete"
       verbosity "8"

rule impl_true_r
  find  %b -> true 
  replace  true 
  tags rewrite "concrete"
       verbosity "8"

rule impl_idempotent
  find  %b -> %b 
  replace  %b 
  tags rewrite "concrete"
       verbosity "8"

(*
 *  stuff with not
 *)

rule not_right
  find |-  !%b 
  samegoal remove
           add  %b  |-
  tags rewrite "prop simp"
       verbosity "8"

rule not_left
  find  !%b  |-
  samegoal remove
           add |-  %b 
  tags rewrite "prop simp"
       verbosity "8"

rule not_true
  find  !true 
  replace  false 
  tags rewrite "concrete"
       verbosity "8"

rule not_false
  find  !false 
  replace  true 
  tags rewrite "concrete"
       verbosity "8"

rule not_not
  find  ! ! %b 
  replace  %b 
  tags rewrite "concrete"
       verbosity "8"

(*
 * stuff with equivalence
 *)

rule equiv_to_eq
  find %a <-> %b
  replace %a = %b
  tags rewrite "prop simp"
       verbosity "8"
