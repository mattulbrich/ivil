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


(*  Dont use prettyprinter.
 * plugin
 *   prettyPrinter : "de.uka.iti.pseudo.parser.boogie.environment.BoogiePrettyPrinter"
 *)
 
(*
 * Toplevel and right will be processed automatically.
 *)
    
rule toplevel_and_right
  find |-  %a & %b 
  where 
    toplevel
  samegoal "Conj1: {%a}"
    replace  %a 
  samegoal "Conj2: {%b}"
    replace  %b 
  tags rewrite "prop simp"
  
(*
  \some
*)
rule some
  find  (\some %x; %b) 
  add $$subst(%x, $$skolem(%x), %b) |-
  replace  $$subst(%x, $$skolem(%x), %x)
  tags rewrite "fol simp"

(*
  $codeexpression
 *)
  
function
  'a $codeexpression(bool, 'a)

rule after_codeexpression
  find $codeexpression(%b, %a)
  where programFree %b
  where noFreeVars %b
  replace %a
  add %b |-
  tags rewrite "concrete"

  
(*
  Treatment of <:
 *)
 
function
  bool  $extends('a, 'a)		infix <: 50
  
rule extends_trans
	find $extends(%a, %b)
	assume $extends(%b, %c) |-
	replace $extends(%a, %c)

# try to move extends formulas to the right side
rule extends_refl
	find $extends(%a, %a)
	replace true
	tags rewrite "concrete"

rule extends_antisym
	find |- $extends(%a, %b)
	where toplevel
	replace !$extends(%b, %a)
	tags rewrite "concrete"

rule extends_sanity
     find $extends(%a, %b) |-
     where toplevel
     assume $extends(%b, %a) |-
     assume |- %a = %b
     replace false
     tags rewrite "concrete"
