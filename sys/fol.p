#
# This file is part of This file is part of
#    ivil - dragdrop Verification on Intermediate Language
#
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains rules for first order logic
 *)

include
   "$proposition.p"

rule forall_right
  find  |-  (\forall %x; %b) 
  replace  $$subst(%x, $$skolem(%x), %b)
  tags rewrite "fol simp"

rule exists_right
  find  |-  (\exists %x; %b) 
  where
    interact %inst
  add |-  $$subst(%x, %inst, %b)
  tags dragdrop "6"
  
rule exists_right_hide
  find  |-  (\exists %x; %b) 
  where
    interact %inst
  replace  $$subst(%x, %inst, %b)
  tags dragdrop "6"
       hiding "find"

rule forall_left
  find  (\forall %x; %b)  |-
  where
    interact %inst
  add $$subst(%x, %inst, %b) |-
  tags dragdrop "6"

rule forall_left_hide
  find  (\forall %x; %b)  |-
  where
    interact %inst
  replace $$subst(%x, %inst, %b)
  tags dragdrop "6"
       hiding "find"

rule exists_left
  find   (\exists %x; %b)  |-
  replace  $$subst(%x, $$skolem(%x), %b)
  tags rewrite "fol simp"

(* type quantifications *)

rule typed_forall_left
  find (\T_all %'a; (\forall %x as %'a; %b)) |-
  where
    interact %inst as %'inst, true
  add $$polymorphicSpec(%x as %'a, %inst, %b, true) |-
  tags dragdrop "7"
  
rule typed_forall_left_hide
  find (\T_all %'a; (\forall %x as %'a; %b)) |-
  where
    interact %inst as %'inst, true
  replace $$polymorphicSpec(%x as %'a, %inst, %b, true)
  tags dragdrop "7"
       hiding "find"

rule type_quant_left
  find (\T_all %'a; %b) |-
  where
    interact %inst as %'inst, true
  add $$polymorphicSpec(arb as %'a, %inst, %b, false) |-
  tags dragdrop "4"

rule type_quant_left_hide
  find (\T_all %'a; %b) |-
  where
    interact %inst as %'inst, true
  replace $$polymorphicSpec(arb as %'a, %inst, %b, false)
  tags dragdrop "4"
       hiding "find"

(*
 * Conditionals
 *)
rule cond_true
  find  cond(true, %a, %b) 
  replace  %a 
  tags rewrite "concrete"
       verbosity "8"

rule cond_false
  find  cond(false, %a, %b) 
  replace  %b
  tags rewrite "concrete"
       verbosity "8"

rule cut_cond
  find cond(%c, %a, %b)
  where 
    toplevel
  where
    noFreeVars(%c)
  samegoal "Assume true for {%c}"
    add %c |-
    replace %a
  samegoal "Assume false for {%c}"
    add |- %c
    replace %b

(*
 * Equality
 *)
rule equality_refl
  find  %t = %t 
  replace true
  tags rewrite "concrete"
       verbosity "8"

rule equality_comm
  find  %t = %u 
  replace %u = %t

(* bring equalities into order if they are not yet 
 * This is only of importance if the term is toplevel
 * in the antecedent since only then it will be applied
 *)
rule equality_order
  find %t = %u |-
  where
    unorderedTerms %t, %u
  replace %u = %t
  tags
    rewrite "fol simp"
    verbosity "8"

rule equality_apply
  find  %t 
  assume  %t = %u  |-
  where
    toplevel
  replace   %u
  tags dragdrop "8"

rule auto_equality_apply
  find %t 
  assume %t = %u  |-
  where toplevel
  where distinctAssumeAndFind
  where not unorderedTerms %t, %u
  replace %u 
  tags
    autoonly
    # rewrite "fol simp"
    verbosity "3"

rule equality_unique
  find %t = %u
  where isUnique %t
  where isUnique %u
  replace $$resolveUnique(%t, %u)
  tags rewrite "fol simp"
       verbosity "8"
