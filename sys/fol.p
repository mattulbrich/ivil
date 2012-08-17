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

(*
 * Quantifier rules:
 *  - delta rules: forall_right, exists_left
 *  - gamma rules: forall_left, exists_right
 *  - removal rules: forall_remove, exists_remove
 *)

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


rule forall_remove
  find (\forall %x; %b)
  where freshVar %x, %b
  replace %b
  tags rewrite "fol simp"

rule exists_remove
  find (\exists %x; %b)
  where freshVar %x, %b
  replace %b
  tags rewrite "fol simp"

(* 
 * universal type quantifications 
 *)

rule typed_forall_left
  find (\T_all %'a; (\forall %x as %'a; %b)) |-
  where
    interact %inst as %'inst, true
  add $$polymorphicSubst(%x, %inst, %b) |-
  tags dragdrop "7"
  (*
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

rule type_quant_right
  find |- (\T_all %'a; %b)
  replace $$polymorphicSpec(arb as %'a, $$skolemType(arb as %'a), %b, false)
  tags rewrite "fol simp"

*)
(* TODO: existential type quantifications *)

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
       
rule cond_known_left
  find  cond(%c, %a, %b) 
  assume %c |-
  replace  %a
  tags rewrite "fol simp"
       verbosity "8"
       
rule cond_known_right
  find  cond(%c, %a, %b) 
  assume |- %c
  replace  %b
  tags rewrite "fol simp"
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
  tags rewrite "split"

(*
 * Weakly typed equality
 *)

plugin
    # check whether two terms have different types, but returns false if typevariables are present
    whereCondition : "de.uka.iti.pseudo.rule.where.DifferentGroundTypes"
 
rule weakly_typed_equality_different_ground_types
  find  $weq(%a,%b)
  where differentGroundTypes %a, %b 
  replace false
  tags rewrite "concrete"
       verbosity "6"
       
rule weakly_typed_equality_same_types
  find  $weq(%a as %'a,%b as %'a)
  replace %a = %b
  tags rewrite "concrete"
       verbosity "6"

rule retype_identity
  find retype(%x as %'a) as %'a
  replace %x
  tags rewrite "concrete"

rule apply_weq_retype
  assume $weq(%a as %'a, %b as %'b) |-
  find retype(%a) as %'b
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
  tags 
    verbosity "6"
    dragdrop "8"

rule equality_apply_reverse
  find %t
  assume %u = %t |-
  where
    toplevel
  replace %u
  tags 
   verbosity "6"
   dragdrop "8"

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
    verbosity "6"

rule equality_unique
  find %t = %u
  where isUnique %t
  where isUnique %u
  replace $$resolveUnique(%t, %u)
  tags rewrite "fol simp"
       verbosity "8"

(*
 * Pattern treatment
 *)
rule remove_pattern
  find %s ~~> %t
  replace %t

rule insert_pattern
  find %t
  where
    interact %s
  replace %s ~~> %t

(*
 * Hilbert operator, epsilon, \some
 *)

rule some
  find (\some %x; %b)
  add (\exists %x; %b) -> $$subst(%x, (\some %x; %b), %b) |-
  