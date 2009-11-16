#
# This file is part of PSEUDO
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

  # the general update simplifier
  metaFunction : "de.uka.iti.pseudo.rule.meta.UpdSimplMetaFunction"

  # check whether a term does not contain modalities
  whereCondition : "de.uka.iti.pseudo.rule.where.ProgramFree"

(*
 * First the theoretical rules
 * They are not applied automatically
 * -- inefficient
 *)

function 
  int $enumerateAssignables('a, 'b)  infix // 50

rule prg_skip
  find [%a : skip] 
  samegoal replace  $$incPrg(%a)
  tags rewrite "symbex"
       display "|> skip"

rule tprg_skip
  find [[%a : skip]] 
  samegoal replace  $$incPrg(%a) 
  tags rewrite "symbex"
       display "|> skip"

rule prg_goto1
  find [%a : goto %n] 
  samegoal replace  $$jmpPrg(%a, %n) 
  tags rewrite "symbex"
       display "|> goto {%n}"

rule tprg_goto1
  find [[%a : goto %n]] 
  samegoal replace  $$jmpPrg(%a, %n) 
  tags rewrite "symbex"
       display "|> goto {%n}"

rule prg_goto2
  find  [%a : goto %n, %k] 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k)
  tags display "|> goto {%n}, {%k}"

rule tprg_goto2
  find  [[%a : goto %n, %k]] 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k) 
  tags display "|> goto {%n}, {%k}"


rule prg_assert
  find  [%a : assert %b]
  samegoal replace %b & $$incPrg(%a)
  tags display "|> assert {%b}"

rule tprg_assert
  find  [[%a : assert %b]]
  samegoal replace %b & $$incPrg(%a)
  tags display "|> assert {%b}"


rule prg_assume
  find [%a : assume %b]
  samegoal replace %b -> $$incPrg(%a)
  tags display "|> assume {%b}"

rule tprg_assume
  find [[%a : assume %b]]
  samegoal replace %b -> $$incPrg(%a)
  tags display "|> assume {%b}"


rule prg_end
  find [%a : end %b]
  samegoal replace %b
  tags rewrite "symbex"
       display "|> end {%b}"

rule tprg_end
  find [[%a : end %b]]
  samegoal replace %b
  tags rewrite "symbex"
       display "|> end {%b}"


rule prg_assignment
  find [%a : %x := %v]
  samegoal replace  { %x := %v }$$incPrg(%a) 
  tags rewrite "symbex"
       display "|> {%x} := {%v}"

rule tprg_assignment
  find [[%a : %x := %v]]
  samegoal replace  { %x := %v }$$incPrg(%a) 
  tags rewrite "symbex"
       display "|> {%x} := {%v}"


rule prg_havoc
  find [%a : havoc %v]
  samegoal replace (\forall x; { %v := x }$$incPrg(%a))
  tags display "|> havoc {%v}"

rule tprg_havoc
  find [[%a : havoc %v]]
  samegoal replace (\forall x; { %v := x }$$incPrg(%a))
  tags display "|> havoc {%v}"

(*
 * Rules for automation
 *
 * Are given only for programs on toplevel succendent
 *)

rule auto_goto2
  find |- [%a : goto %n, %k]
  samegoal "goto {%n}"
    replace $$jmpPrg(%a, %n) 
  samegoal "goto {%k}"
    replace $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"

rule auto_goto2_upd
  find |- {U} [%a : goto %n, %k]
  samegoal "goto {%n}"
    replace {U} $$jmpPrg(%a, %n) 
  samegoal "goto {%k}"
    replace {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"

rule autot_goto2
  find |- [[%a : goto %n, %k]]
  samegoal "goto {%n}"
    replace $$jmpPrg(%a, %n) 
  samegoal "goto {%}"
    replace $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"

rule autot_goto2_upd
  find |- {U} [[%a : goto %n, %k]]
  samegoal "goto {%n}"
    replace {U} $$jmpPrg(%a, %n) 
  samegoal "goto {%k}"
    replace {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"
       display "|> goto {%n}, {%k}"



rule auto_assert
  find |- [%a : assert %b]
  samegoal  "assert {%b}"
    replace %b 
  samegoal "continue program"
    replace $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}"

rule autot_assert
  find |- [[%a : assert %b]]
  samegoal "assert {%b}"
    replace %b 
  samegoal "continue program"
    replace $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}"

rule auto_assert_upd
  find |- {U} [%a : assert %b]
  samegoal "assert {%b}"
    replace {U} %b 
  samegoal "continue program"
    replace {U} $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}"

rule autot_assert_upd
  find |- {U} [[%a : assert %b]]
  samegoal "assert {%b}"
    replace {U} %b 
  samegoal "continue program"
    replace {U} $$incPrg(%a)
  tags rewrite "symbex"
       display "|> assert {%b}"


rule auto_assume
  find |- [%a : assume %b]
  samegoal 
    replace $$incPrg(%a)
    add %b |-
  tags rewrite "symbex"
       display "|> assume {%b}"

rule autot_assume
  find |- [%a : assume %b]
  samegoal 
    replace $$incPrg(%a)
    add %b |-
  tags rewrite "symbex"
       display "|> assume {%b}"

rule auto_assume_upd
  find |- {U} [%a : assume %b]
  samegoal 
    replace {U} $$incPrg(%a)
    add {U} %b |-
  tags rewrite "symbex"
       display "|> assume {%b}"

rule autot_assume_upd
  find |- {U} [%a : assume %b]
  samegoal 
    replace {U} $$incPrg(%a)
    add {U} %b |-
  tags rewrite "symbex"
       display "|> assume {%b}"


rule auto_havoc
  find |- [%a : havoc %v]
  samegoal replace { %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}"

rule autot_havoc
  find |- [%a : havoc %v]
  samegoal replace { %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}"

rule auto_havoc_upd
  find |- {U} [%a : havoc %v]
  samegoal replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}"

rule autot_havoc_upd
  find |- {U} [%a : havoc %v]
  samegoal replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"
       display "|> havoc {%v}"

(*
 * loop invariant rules
 *)

rule loop_invariant
  find |- [%a]
  where
    interact %inv
  samegoal "inv initially valid"
    replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, 0)
  tags
    display "invariant in {%a}"

rule loop_invariant_update
  find |- {U}[%a]
  where
    interact %inv
  samegoal "inv initially valid" replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, 0)
  tags
    display "invariant in {%a}"


rule auto_loop_invariant_update
  find |- {U}[%a : skip_loopinv %inv]
  samegoal "inv initially valid" replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, 0)
  tags rewrite "symbex"
       display "invariant in {%a}: {explain %a}"

(*
 * Update simplification
 *)
rule update_simplification
  find {U}%t
  where canEval $$updSimpl({U}%t)
  samegoal replace $$updSimpl({U}%t)
  tags rewrite "updSimpl"
       verbosity "10"
