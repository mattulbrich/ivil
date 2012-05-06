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
 * This file contains rules for symbolic execution of unstructered
 * programs
 *)

include
   "$fol.p"

plugin
  # increment program counter
  metaFunction : "de.uka.iti.pseudo.rule.meta.IncPrgMetaFunction"

  # jump to new program counter
  metaFunction : "de.uka.iti.pseudo.rule.meta.JmpPrgMetaFunction"

  # the loop invariant program changements
  metaFunction : "de.uka.iti.pseudo.rule.meta.LoopInvariantProgramModificationMetaFunction"

  # the general single-step update simplifier
  metaFunction : "de.uka.iti.pseudo.rule.meta.UpdSimplMetaFunction"

  # the general deep update simplifier
  metaFunction : "de.uka.iti.pseudo.rule.meta.DeepUpdSimplMetaFunction"
  
  # process an update statment
  #metaFunction : "de.uka.iti.pseudo.rule.meta.ApplyUpdateStatement"

  # check whether a term does not contain modalities
  whereCondition : "de.uka.iti.pseudo.rule.where.ProgramFree"
  
  # check whether a term is a Program at an updateAssignment position
#  whereCondition : "de.uka.iti.pseudo.rule.where.IsUpdateStatement"

(*
 * First the theoretical rules
 * They are not applied automatically
 * -- inefficient
 *)

function 
  int $enumerateAssignables('a, 'b)  infix // 50

rule prg_skip
  find [? %a : skip ?]%phi
  samegoal replace  $$incPrg(%a)
  tags rewrite "symbex"
       display "|> skip"

rule prg_goto1
  find [? %a : goto %n ?]%phi
  samegoal replace  $$jmpPrg(%a, %n) 
  tags rewrite "symbex"
       display "|> goto {%n}"


rule prg_goto2
  find  [ %a : goto %n, %k ]%phi 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k)
  tags display "|> goto {%n}, {%k}"

rule tprg_goto2
  find  [[%a : goto %n, %k]]%phi 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k) 
  tags display "|> goto {%n}, {%k}"

rule dprg_goto2
  find  [< %a : goto %n, %k >]%phi 
  samegoal replace  $$jmpPrg(%a, %n) | $$jmpPrg(%a, %k) 
  tags display "|> goto {%n}, {%k}"


rule prg_assert
  find  [%a : assert %b]%phi
  samegoal replace %b & $$incPrg(%a)
  tags display "|> assert {%b}: {explain %a}"

rule tprg_assert
  find  [[%a : assert %b]]%phi
  samegoal replace %b & $$incPrg(%a)
  tags display "|> assert {%b}: {explain %a}"

rule dprg_assert
  find  [< %a : assert %b >]%phi
  samegoal replace %b -> $$incPrg(%a)
  tags display "|> assert {%b}: {explain %a}"


rule prg_assume
  find [%a : assume %b]%phi
  samegoal replace %b -> $$incPrg(%a)
  tags display "|> assume {%b}: {explain %a}"

rule tprg_assume
  find [[%a : assume %b]]%phi
  samegoal replace %b -> $$incPrg(%a)
  tags display "|> assume {%b}: {explain %a}"

rule dprg_assume
  find [<%a : assume %b>]%phi
  samegoal replace %b & $$incPrg(%a)
  tags display "|> assume {%b}: {explain %a}"


rule prg_end
  find [? %a : end ?]%b
  samegoal replace %b
  tags rewrite "symbex"
       display "|> end: {explain %a}"


rule prg_assignment
  find [? %a : U ?]%phi
  samegoal replace  {U}$$incPrg(%a) 
  tags rewrite "symbex"
       display "|> {upd U}"


rule prg_havoc
  find [%a : havoc %v]%phi
  where freshVar %x, %phi
  samegoal replace (\forall %x; { %v := %x }$$incPrg(%a))
  tags display "|> havoc {%v}: {explain %a}"

rule tprg_havoc
  find [[%a : havoc %v]]%phi
  where freshVar %x, %phi
  samegoal replace (\forall %x; { %v := %x }$$incPrg(%a))
  tags display "|> havoc {%v}: {explain %a}"

rule dprg_havoc
  find [<%a : havoc %v>]%phi
  where freshVar %x, %phi
  samegoal replace (\exists %x; { %v := %x }$$incPrg(%a))
  tags display "|> havoc {%v}: {explain %a}"

(*
 * Rules for automation
 *
 * Are given only for programs on toplevel succendent
 *)

rule auto_box_goto2
  find |- { U ?} [%a : goto %n, %k]%phi
  samegoal "goto {%n}"
    replace {U} $$jmpPrg(%a, %n) 
  samegoal "goto {%k}"
    replace {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"

rule auto_tbox_goto2
  find |- { U ?} [[%a : goto %n, %k]]%phi
  samegoal "goto {%n}"
    replace {U} $$jmpPrg(%a, %n) 
  samegoal "goto {%k}"
    replace {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"

rule auto_dia_goto2
  find |- { U ?} [<%a : goto %n, %k>]%phi
  samegoal "goto {%n}, {%k}"
    replace {U} $$jmpPrg(%a, %n) 
    add |- {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"


rule auto_box_assert
  find |- { U ?} [%a : assert %b]%phi
  samegoal "{explainOrQuote %a}"
    replace {U} %b 
  samegoal "..."
    replace {U} $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}: {explain %a}"
       hintsOnBranches "0"

rule auto_tbox_assert
  find |- { U ?} [[%a : assert %b]]%phi
  samegoal "{explainOrQuote %a}"
    replace {U} %b 
  samegoal "..."
    replace {U} $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}: {explain %a}"
       hintsOnBranches "0"

rule auto_dia_assert
  find |- { U ?} [<%a : assert %b>]%phi
  samegoal
    add {U} %b |-
    replace {U} $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}: {explain %a}"
       hintsOnBranches "0"


rule auto_box_assume
  find |- { U ?} [%a : assume %b]%phi
  samegoal
    replace {U} $$incPrg(%a)
    add {U} %b |-
  tags rewrite "symbex"
       display "|> assume {%b}: {explain %a}"

rule auto_tbox_assume
  find |- { U ?} [[%a : assume %b]]%phi
  samegoal
    replace {U} $$incPrg(%a)
    add {U} %b |-
  tags rewrite "symbex"
       display "|> assume {%b}: {explain %a}"

rule auto_dia_assume
  find |- { U ?} [<%a : assume %b>]%phi
  samegoal "{explainOrQuote %a}"
    replace {U} %b 
  samegoal "..."
    replace {U} $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}: {explain %a}"


rule auto_box_havoc
  find |- { U ?} [%a : havoc %v]%phi
  samegoal replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}: {explain %a}"

rule auto_tbox_havoc
  find |- { U ?} [[%a : havoc %v]]%phi
  replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}: {explain %a}"

(*
rule auto_dia_havoc
  find |- { U ?} [<%a : havoc %v>]%phi
  replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}: {explain %a}"
*)     

(*
 * loop invariant rules
 *)

rule loop_invariant
  find |- [%a]%phi
  where
    interact %inv
  samegoal "inv initially valid"
    replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, 0)
  tags
    display "invariant in {%a}: {explain %a}"
    interactive "5"

rule loop_invariant_update
  find |- {U}[%a]%phi
  where
    interact %inv
  samegoal "inv initially valid" replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, 0)
  tags
    display "invariant in {%a}: {explain %a}"
    interactive "5"

rule loop_invariant_t
  find |- [[%a]]%phi
  where
    interact %inv
  where
    interact %var
  samegoal "inv initially valid"
    replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, %var)
  tags
    display "invariant in {%a}: {explain %a}"
    dragdrop "5"

rule loop_invariant_update_t
  find |- {U}[[%a]]%phi
  where
    interact %inv
  where
    interact %var  samegoal "inv initially valid" replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, %var)
  tags
    display "invariant in {%a}: {explain %a}"
    dragdrop "5"

rule auto_loop_invariant
  find |- [%a : skip_loopinv %inv]%phi
  samegoal "inv initially valid" 
    replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, 0)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

rule auto_loop_invariant_update
  find |- {U}[%a : skip_loopinv %inv]%phi
  samegoal "inv initially valid"
    replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, 0)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

rule auto_loop_invariant_variant
  find |- [%a : skip_loopinv %inv, %var]%phi
  samegoal "inv initially valid" 
    replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, %var)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

rule auto_loop_invariant_variant_update
  find |- {U}[%a : skip_loopinv %inv, %var]%phi
  samegoal "inv initially valid" 
    replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, %var)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

rule autot_loop_invariant
  find |- [[%a : skip_loopinv %inv, %var]]%phi
  samegoal "inv initially valid" 
    replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, %var)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

rule autot_loop_invariant_update
  find |- {U}[[%a : skip_loopinv %inv, %var]]%phi
  samegoal "inv initially valid" 
    replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, %var)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

(*
 * Update simplification
 *)
(*
rule update_simplification
  find {U}%t
  where canEval $$updSimpl({U}%t)
  samegoal replace $$updSimpl({U}%t)
  tags rewrite "updSimpl"
       verbosity "10"
*)

rule deep_update_simplification
  find { U }%t
  where canEval $$deepUpdSimpl({U}%t)
  samegoal replace $$deepUpdSimpl({U}%t)
  tags rewrite "updSimpl"
       verbosity "10"
