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

function 
  int $enumerateAssignables('a, 'b)  infix // 50

rule prg_skip
  find [%a : skip] 
  samegoal replace  $$incPrg(%a) 
#  tags rewrite "symbex"

rule tprg_skip
  find [[%a : skip]] 
  samegoal replace  $$incPrg(%a) 
#  tags rewrite "symbex"


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
  tags rewrite "symbex"

rule tprg_goto2
  find  [[%a : goto %n, %k]] 
  samegoal replace  $$jmpPrg(%a, %n) & $$jmpPrg(%a, %k) 
  tags rewrite "symbex"


rule prg_assert
  find  [%a : assert %b]
  samegoal replace %b & $$incPrg(%a)
  tags rewrite "symbex"

rule tprg_assert
  find  [[%a : assert %b]]
  samegoal replace %b & $$incPrg(%a)
  tags rewrite "symbex"


rule prg_assume
  find [%a : assume %b]
  samegoal replace %b -> $$incPrg(%a)
  tags rewrite "symbex"

rule tprg_assume
  find [[%a : assume %b]]
  samegoal replace %b -> $$incPrg(%a)
  tags rewrite "symbex"


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
  tags rewrite "symbex"

rule tprg_havoc
  find [[%a : havoc %v]]
  samegoal replace (\forall x; { %v := x }$$incPrg(%a))
  tags rewrite "symbex"


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
  find %t
  where canEval $$updSimpl(%t)
  samegoal replace $$updSimpl(%t)
  tags rewrite "updSimpl"
