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

rule prg_skip
  find [%a : skip] 
  samegoal replace  $$incPrg(%a) 
  tags rewrite "symbex"

rule tprg_skip
  find [[%a : skip]] 
  samegoal replace  $$incPrg(%a) 
  tags rewrite "symbex"


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
  samegoal replace  $$jmpPrg(%a, %n)
  samegoal replace  $$jmpPrg(%a, %k) 
  tags rewrite "symbex"

rule tprg_goto2
  find  [[%a : goto %n, %k]] 
  samegoal replace  $$jmpPrg(%a, %n)
  samegoal replace  $$jmpPrg(%a, %k) 
  tags rewrite "symbex"


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

rule tprg_end
  find [[%a : end %b]]
  samegoal replace %b


rule prg_assignment
  find [%a : %x := %v]
  samegoal replace  { %x := %v }$$incPrg(%a) 
  tags rewrite "symbex"

rule tprg_assignment
  find [[%a : %x := %v]]
  samegoal replace  { %x := %v }$$incPrg(%a) 
  tags rewrite "symbex"


rule update_simplification
  find %t
  where canEval $$updSimpl(%t)
  samegoal replace $$updSimpl(%t)
