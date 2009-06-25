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

rule tprg_skip
  find [[%a : skip]] 
  samegoal replace  $$incPrg(%a) 


rule prg_goto1
  find [%a : goto %n] 
  samegoal replace  $$jmpPrg(%a, %n) 
  tags rewrite "symbex"

rule tprg_goto1
  find [[%a : goto %n]] 
  samegoal replace  $$jmpPrg(%a, %n) 
  tags rewrite "symbex"


rule prg_goto2
  find  [%a : goto %n, %k] 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k) 

rule tprg_goto2
  find  [[%a : goto %n, %k]] 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k) 


rule prg_assert
  find  [%a : assert %b]
  samegoal replace %b & $$incPrg(%a)

rule tprg_assert
  find  [[%a : assert %b]]
  samegoal replace %b & $$incPrg(%a)


rule prg_assume
  find [%a : assume %b]
  samegoal replace %b -> $$incPrg(%a)

rule tprg_assume
  find [[%a : assume %b]]
  samegoal replace %b -> $$incPrg(%a)


rule prg_end
  find [%a : end %b]
  samegoal replace %b
  tags rewrite "symbex"

rule tprg_end
  find [[%a : end %b]]
  samegoal replace %b
  tags rewrite "symbex"


rule prg_assignment
  find [%a : %x := %v]
  samegoal replace  { %x := %v }$$incPrg(%a) 
  tags rewrite "symbex"

rule tprg_assignment
  find [[%a : %x := %v]]
  samegoal replace  { %x := %v }$$incPrg(%a) 
  tags rewrite "symbex"


rule prg_havoc
  find [%a : havoc %v]
  samegoal replace (\forall x; { %v := x }$$incPrg(%a))

rule tprg_havoc
  find [[%a : havoc %v]]
  samegoal replace (\forall x; { %v := x }$$incPrg(%a))


(*
 * Rules for automation
 * 
 * Are given only for programs on toplevel succendent
 *)

rule auto_goto2
  find |- [%a : goto %n, %k]
  samegoal replace $$jmpPrg(%a, %n) 
  samegoal replace $$jmpPrg(%a, %k)
  tags rewrite "symbex"

rule auto_goto2_upd
  find |- {U} [%a : goto %n, %k]
  samegoal replace {U} $$jmpPrg(%a, %n) 
  samegoal replace {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"

rule autot_goto2
  find |- [[%a : goto %n, %k]]
  samegoal replace $$jmpPrg(%a, %n) 
  samegoal replace $$jmpPrg(%a, %k)
  tags rewrite "symbex"

rule autot_goto2_upd
  find |- {U} [[%a : goto %n, %k]]
  samegoal replace {U} $$jmpPrg(%a, %n) 
  samegoal replace {U} $$jmpPrg(%a, %k)
  tags rewrite "symbex"



rule auto_assert
  find |- [%a : assert %b]
  samegoal replace %b 
  samegoal replace $$incPrg(%a)
  tags rewrite "symbex"

rule autot_assert
  find |- [[%a : assert %b]]
  samegoal replace %b 
  samegoal replace $$incPrg(%a)
  tags rewrite "symbex"

rule auto_assert_upd
  find |- {U} [%a : assert %b]
  samegoal replace {U} %b 
  samegoal replace {U} $$incPrg(%a)
  tags rewrite "symbex"

rule autot_assert_upd
  find |- {U} [[%a : assert %b]]
  samegoal replace {U} %b 
  samegoal replace {U} $$incPrg(%a)
  tags rewrite "symbex"


rule auto_assume
  find |- [%a : assume %b]
  samegoal 
    replace $$incPrg(%a)
    add %b |-
  tags rewrite "symbex"

rule autot_assume
  find |- [%a : assume %b]
  samegoal 
    replace $$incPrg(%a)
    add %b |-
  tags rewrite "symbex"

rule auto_assume_upd
  find |- {U} [%a : assume %b]
  samegoal 
    replace {U} $$incPrg(%a)
    add {U} %b |-
  tags rewrite "symbex"

rule autot_assume_upd
  find |- {U} [%a : assume %b]
  samegoal 
    replace {U} $$incPrg(%a)
    add {U} %b |-
  tags rewrite "symbex"


rule auto_havoc
  find |- [%a : havoc %v]
  samegoal replace { %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"

rule autot_havoc
  find |- [%a : havoc %v]
  samegoal replace { %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"

rule auto_havoc_upd
  find |- {U} [%a : havoc %v]
  samegoal replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"

rule autot_havoc_upd
  find |- {U} [%a : havoc %v]
  samegoal replace {U}{ %v := $$skolem(%v) }$$incPrg(%a)
  tags rewrite "symbex"


(*
 * loop invariant rules
 *)

rule loop_invariant
  find |- [%a]
  where
    interact %inv
  where
    interact %modifies as int
  samegoal "inv initially valid" replace %inv
  samegoal "run with cut program" 
    replace $$loopInvPrgMod(%a, %inv, 0, %modifies)



rule loop_invariant_update
  find |- {U}[%a]
  where
    interact %inv
  where
    interact %modifies as int
  samegoal "inv initially valid" replace {U}%inv
  samegoal "run with cut program" 
    replace {U}$$loopInvPrgMod(%a, %inv, 0, %modifies)


rule update_simplification
  find {U}%t
  where canEval $$updSimpl({U}%t)
  samegoal replace $$updSimpl({U}%t)
  tags rewrite "updSimpl"
