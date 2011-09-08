#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2010 Universitaet Karlsruhe, Germany
#    written by Timm Felden
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains the very basic definitions needed for treatment of boogie
 * files.
 *)
 
include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"
  "$unicode.p"
  "$fol.p"
  "$bitvector.p"
  "$map.p"


(*  Dont use the pretty printer. *)
 plugin
   prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.BoogiePrettyPrinter"
  
(*
  \some
*)
rule some
  find  (\some %x; %b) 
  where programFree %b           # this is needed as the program may contain %x and as the program would be duplicated if not evaluated first leading to unnecessary work duplation
  add (\exists %x; %b) -> $$subst(%x, $$skolem(%x), %b) |-
  replace  $$subst(%x, $$skolem(%x), %x)
  tags rewrite "fol simp"

  
(*
  Treatment of <:
 *)
 
function
  bool  $extends_direct('a, 'a)
  bool  $extends_unique('a, 'a)
  bool  $extends('a, 'a)		infix <: 50

# extends_direct <-> (a <: b & \forall x ; (a==x | b==x) <-> (a <: x & x <: b))
axiom extends_direct_simplification
  (\T_all 't; (\forall a as 't; (\forall b; $extends_direct(a, b) <-> ( !a=b & a<:b & (\forall x; (a=x|b=x) <-> (a<:x & x<:b))) )))

rule extends_direct
	find $extends_direct(%a, %b)
	replace !%a=%b & %a<:%b & (\forall x; (%a=x|%b=x)<->(%a<:x & x<:%b))
	tags rewrite "fol simp"



axiom extends_unique_simplification
  (\T_all 't; (\forall a as 't; (\forall b; $extends_unique(a, b) -> $extends_direct(a, b))))

# for a human user this is easier to use than instantiating the axiom above
rule extends_unique_to_direct
     find $extends_unique(%a, %b)
     replace $extends_unique(%a, %b) & $extends_direct(%a, %b)


rule extends_unique_false
    find %a = %b
    assume $extends_unique(%A, %p) |-
    assume $extends_unique(%B, %p) |-
    assume |- %A = %B
    assume %a <: %A |-
    assume %b <: %B |-
    replace false
    
rule extends_unique_false_assume
    find %a = %b
    assume $extends_unique(%A, %P1) |-
    assume $extends_unique(%B, %P2) |-
    assume %P1 = %P2 |-
    assume |- %A = %B
    assume %a <: %A |-
    assume %b <: %B |-
    replace false

# transitivity for extends and inequality, as extends is acyclic
rule extends_trans
	find $extends(%a, %b)
	assume $extends(%b, %c) |-
	replace $extends(%a, %b) & $extends(%a, %c)
	
rule extends_trans_inequality_a
	find $extends(%a, %b)
	assume $extends(%b, %c) |-
	assume |- %b = %c
	replace $extends(%a, %b) & !%a = %c
	
rule extends_trans_inequality_b
	find $extends(%a, %b)
	assume $extends(%b, %c) |-
	assume |- %a = %b
	replace $extends(%a, %b) & !%a = %c

#extends is reflexive
rule extends_refl
	find $extends(%a, %a)
	replace true
	tags rewrite "concrete"
	
rule extends_refl_assume
	find $extends(%a, %b)
	assume %a = %b |-
	replace true
	tags rewrite "concrete"

# try to move extends formulas to the left side
rule extends_antisym_auto
	find |- $extends(%a, %b)
	where toplevel
	replace !$extends(%b, %a)
	tags rewrite "concrete"

rule extends_antisym
	find $extends(%a, %b)
	assume |- %a = %b
	where toplevel
	replace !$extends(%b, %a)
	
rule extends_antisym_close
    find $extends(%a, %b) |-
    assume $extends(%b, %a) |-
	assume |- %a = %b
	where toplevel
    closegoal
    tags rewrite "close"

#if a cycle is found, break the cycle and replace the definition of one edge by false
rule extends_sanity
     find $extends(%a, %b) |-
     assume $extends(%b, %a) |-
     assume |- %a = %b
     replace false
     tags rewrite "concrete"
