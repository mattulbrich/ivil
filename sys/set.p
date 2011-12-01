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
 * This files contains the optimised rules for sets
 *
 * You find definitions in setdefs.p
 *)

include "$setdefs.p"
include "$int.p"
plugin
  prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.SetPrettyPrinter"

(*
 * rules with emptyset
 *)

rule emptyset_in_anyset
find emptyset <: %s
replace true
tags 
  rewrite "concrete"
  asAxiom 
  derived

rule nothing_in_emptyset
find %x :: emptyset
replace false
tags 
  rewrite "concrete"
  asAxiom 
  derived

rule emptyset_equals
find emptyset = %s
where freshVar %x, %s
replace (\forall %x; !%x::%s)
tags
  rewrite "concrete"
  #asAxiom
  derived

(*
 * rules with fullset
 *)

rule anyset_in_fullset
find %s <: fullset
replace true
tags
  rewrite "concrete"
  asAxiom
  derived

rule anything_in_fullset
find %x :: fullset
replace true
tags
  rewrite "concrete"
  asAxiom
  derived

rule fullset_equals
find fullset = %s
where freshVar %x, %s
replace (\forall %x; %x::%s)
tags
  rewrite "concrete"
  #asAxiom
  derived
  
(*
 * rules with singleton
 *)

rule in_singleton
find %x :: singleton(%y)
replace %x=%y
tags
  rewrite "fol simp"
  asAxiom
  derived

rule subset_singleton
find %x <: singleton(%y)
replace %x=emptyset | %x=singleton(%y)

rule singleton_eq_singleton
find singleton(%x) = singleton(%y)
replace %x=%y
tags
  rewrite "fol simp"
  asAxiom
  derived

(*
 * rules with diff
 *)

rule in_diff
find %x :: %a \ %b
replace %x::%a & !%x::%b
tags
  rewrite "fol simp"
  derived

rule diff_is_conj
find %x \ %y
replace %x /\ ^%y
tags
  derived

(*
 * rules with subset
 *)

rule subset_def
find %a <: %b
where
  freshVar %x, %a, %b
replace (\forall %x; %x :: %a -> %x :: %b)
 
rule subset_refl
find %a <: %a
replace true
tags
  rewrite "fol simp"
  derived

rule subset_trans
find %x::%s |-
assume %s <: %t |-
add %x :: %t |-
tags
  rewrite "fol add"
  derived
  
(*
 * rules with union
 *)
 
rule union_empty_l
find emptyset \/ %a
replace %a
tags
  rewrite "fol simp"
  derived
  
rule union_empty_r
find %a \/ emptyset
replace %a
tags
  rewrite "fol simp"
  derived

rule in_union
find %x :: %a \/ %b
replace %x::%a | %x::%b

rule union_subset
assume %a <: %c |-
find %a /\ %b <: %c
replace true

(*
 * rules with intersect
 *)
 
rule intersect_empty_l
find emptyset /\ %a
replace emptyset
tags
  rewrite "concrete"
  derived
  
rule intersect_empty_r
find %a /\ emptyset
replace emptyset
tags
  rewrite "concrete"
  derived

rule in_intersect
find %x :: %a /\ %b
replace %x :: %a & %x :: %b
tags
  derived
  

(*
 * rules with complement
 *)
rule in_complement
find %x :: ^%s
replace !%x :: %s
tags
  rewrite "fol simp"
  derived

(*
 * rules with equality
 *)
# TODO save variable name
rule set_equality
find %a = %b
replace (\forall ii; ii::%a <-> ii::%b)
tags
  derived

(*
 * rules with \set
 *)
rule in_setext
find %a :: (\set %x; %b)
replace $$subst(%x, %a, %b)


(*
 * rules for finiteness
 *)
rule finite_singleton
find finite(singleton(%a))
replace true
tags
  rewrite "concrete"
  asAxiom

rule finite_emptyset
find finite(emptyset)
replace true
tags
  rewrite "concrete"
  asAxiom

(* This rule is overapproximating, not confluent! *)
rule finite_conj
find |- finite(%s /\ %t)
replace finite(%s) | finite(%t)
tags
  rewrite "fol simp"
  asAxiom

rule finite_disj
find finite(%s \/ %t)
replace finite(%s) & finite(%t)
tags
  rewrite "fol simp"
  asAxiom

(* This rule is overapproximating, not confluent! *)
rule finite_setminus
find |- finite(%s \ %t)
replace finite(%s)
tags
  rewrite "fol simp"
  asAxiom

(* finite fullset implies finite set *)
rule finite_fullset
assume finite(fullset as set(%'a)) |-
find finite(%s as set(%'a))
replace true
tags
  rewrite "fol simp"

(*
 * rules for cardinality
 *)

rule card_emptyset
find card(emptyset)
replace 0
tags
  rewrite "concrete"
  asAxiom
  
rule card_singleton
find card(singleton(%a))
replace 1
tags
  rewrite "concrete"
  asAxiom

rule card_setminus_singleton
assume finite(%s) |-
find card(%s \ singleton(%x))
where 
replace card(%s) - cond(%x :: %s, 1, 0)


  