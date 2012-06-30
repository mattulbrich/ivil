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
 * rules with equality
 *)
rule set_equality
  find %a = %b
  where freshVar %e, %a, %b
  replace (\forall %e; %e::%a <-> %e::%b)
  tags
    derived

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
  verbosity "8"

rule nothing_in_emptyset
find %x :: emptyset
replace false
tags 
  rewrite "concrete"
  asAxiom 
  derived
  verbosity "8"

rule emptyset_equals
find emptyset = %s
where freshVar %x, %s
replace (\forall %x; !%x::%s)
tags
  rewrite "concrete"
  #asAxiom
  derived
  verbosity "6"

rule emptyset_equals2
find %s = emptyset
where freshVar %x, %s
replace (\forall %x; !%x::%s)
tags
  rewrite "concrete"
  #asAxiom
  derived
  verbosity "6"

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
  verbosity "8"

rule anything_in_fullset
find %x :: fullset
replace true
tags
  rewrite "concrete"
  asAxiom
  derived
  verbosity "8"

rule fullset_equals
find fullset = %s
where freshVar %x, %s
replace (\forall %x; %x::%s)
tags
  rewrite "concrete"
  #asAxiom
  derived
  verbosity "6"
  
(*
 * rules with singleton
 *)

rule in_singleton
find %x :: singleton(%y)
replace %x=%y
tags
  rewrite "fol simp"
  derived
  verbosity "8"

rule subset_singleton
find %x <: singleton(%y)
replace %x=emptyset | %x=singleton(%y)
tags
  rewrite "fol simp"
  derived
  verbosity "8"

rule singleton_eq_singleton
find singleton(%x) = singleton(%y)
replace %x=%y
tags
  rewrite "fol simp"
  asAxiom
  derived
  verbosity "8"

(*
 * rules with diff
 *)

rule in_diff
find %x :: %a \ %b
replace %x::%a & !%x::%b
tags
  rewrite "fol simp"
  derived
  verbosity "6"

rule diff_is_conj
find %x \ %y
replace %x /\ ^%y
tags
  derived

rule diff_emptyset
find %s \ emptyset
replace %s
tags
  rewrite "concrete"
  derived
  verbosity "8"

rule diff_is_empty
find %s \ %t = emptyset
replace %s <: %t
tags
  derived

rule diff_same
find %s \ %s
replace emptyset
tags
  derived
  rewrite "concrete"

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
  asAxiom
  verbosity "8"

rule subset_trans
find %x::%s |-
assume %s <: %t |-
where
  not presentInAntecedent %x :: %t
add %x :: %t |-
tags
  rewrite "fol add"
  derived
  verbosity "6"

rule setminus_subset_is_subset
  find %a \ %b <: %c
  assume %a <: %c |-
  where toplevel
  replace true
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

rule subset_of_union
  assume  %a <: %b |-
  find %a <: %b \/ %c
  where toplevel
  replace true
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

(* $prec is real subset on finite sets *)
rule prec_set
  find %a &< (%b as set(%'x))
  where freshVar %x, %a, %b
  replace %a <: %b & (\exists %x; %x :: %b \ %a) & finite(%b)
  tags
    rewrite "fol simp"
    verbosity "8"

(*
 * rules with union
 *)
 
rule union_empty_l
  find emptyset \/ %a
  replace %a
  tags
    rewrite "concrete"
    derived
    verbosity "8"
  
rule union_empty_r
  find %a \/ emptyset
  replace %a
  tags
    rewrite "concrete"
    derived
    verbosity "8"

rule in_union
  find %x :: %a \/ %b
  replace %x::%a | %x::%b

(*
 * rules with intersect
 *)
 
rule intersect_empty_l
  find emptyset /\ %a
  replace emptyset
  tags
    rewrite "concrete"
    derived
    verbosity "8"
  
rule intersect_empty_r
  find %a /\ emptyset
  replace emptyset
  tags
    rewrite "concrete"
    derived
    verbosity "8"

rule in_intersect
  find %x :: %a /\ %b
  replace %x :: %a & %x :: %b
  tags
    derived

rule intersection_subset
  assume %a <: %c |-
  find %a /\ %b <: %c
  where toplevel
  replace true
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

(*
 * rules with complement
 *)
rule in_complement
  find %x :: ^%s
  replace !%x :: %s
  tags
    rewrite "fol simp"
    derived
    verbosity "8"

rule complement_disj
  find ^(%s \/ %t)
  replace ^%s /\ ^%t
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

rule complement_conj
  find ^(%s /\ %t)
  replace ^%s \/ ^%t
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

rule complement_subset
  find ^%s <: ^%t
  replace %t <: %s
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

rule complement_equality
  find ^%s = ^%t
  replace %s = %t
  tags
    rewrite "fol simp"
    derived
    verbosity "6"

(*
 * rules with \set
 *)
rule in_setext
  find %a :: (\set %x; %b)
  replace $$subst(%x, %a, %b)

(*
 * rules for seqAsSet
 *)
rule seqAsSetDef
  find seqAsSet(%s)
  where freshVar %x, %s
  where freshVar %i, %s
  replace (\set %x; (\exists %i; 0<=%i & %i<seqLen(%s) & 
                          seqGet(%s,%i)=%x))

rule inSeqAsSet
  find %x :: seqAsSet(%s)
  where freshVar %i, %s
  replace
    (\exists %i; 0<=%i & %i < seqLen(%s) & seqGet(%s, %i) = %x)
  tags
    derived
    rewrite "fol simp"

rule seqAsSetEmpty
  find seqAsSet(seqEmpty)
  replace emptyset
  tags 
    derived
    rewrite "concrete"

rule finiteSeqAsSet
  find finite(seqAsSet(%s))
  replace true
  tags
    derived
    rewrite "concrete"

# TODO This cannot yet be proved
# cardinality is not defined
rule cardSeqAsSet
  find card(seqAsSet(%s))
  replace seqLen(%s)
  tags
    # derived
    rewrite "fol simp"

(*
 * rules for finiteness
 *)
rule finite_singleton
  find |- finite(singleton(%a))
  replace true
  tags
    rewrite "concrete"
    asAxiom
    verbosity "8"

rule finite_emptyset
  find |- finite(emptyset)
  replace true
  tags
    rewrite "concrete"
    asAxiom
    verbosity "8"

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
    verbosity "6"

(* finite fullset implies finite set *)
rule finite_fullset
  assume finite(fullset as set(%'a)) |-
  find finite(%s as set(%'a))
  where distinctAssumeAndFind
  replace true
  tags
    rewrite "fol simp"
    verbosity "8"

(*
 * rules for cardinality
 *)

rule card_emptyset
  find card(emptyset)
  replace 0
  tags
    rewrite "concrete"
    asAxiom
    verbosity "8"
  
rule card_singleton
  find card(singleton(%a))
  replace 1
  tags
    rewrite "concrete"
    asAxiom
    verbosity "8"

rule card_setminus_singleton
  assume finite(%s \ singleton(%x)) |-
  find card(%s \ singleton(%x))
  replace card(%s) - cond(%x :: %s, 1, 0)


axiom cut_finiteness
  (\T_all 'a; (\forall s as set('a); 
     finite(fullset as set('a)) -> finite(s)))

rule cut_finiteness
  find card(%s)
  where not presentInSequent finite(%s)
  samegoal "Show finite({%s})"
    add |- finite(%s)
  samegoal "..."
    add finite(%s) |-
    
#tags
#  rewrite "fol add"


axiom card_non_negative
  (\T_all 'a; (\forall s as set('a); card(s) >= 0))
  
rule card_non_negative
  find card(%s)
  add card(%s) >= 0 |-

rule card_minus
  assume finite(%s) |-
  assume finite(%t) |-
  find |- card(%s) - card(%t) >= 0
  replace %t <: %s
