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

# TODO!! Replace with fresh variable!
rule emptyset_equals
find emptyset = %s
replace (\forall xx; !xx::%s)
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
replace (\forall yy; yy :: %a -> yy :: %b)
 
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