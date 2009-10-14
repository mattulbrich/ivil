#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains infrastructure for stack manipulation.
 *)

include
   "$fol.p"
   "$heap.p"

sort
  stack
  depth

function
  depth ZERO
  depth D(depth)

  stack empty
  stack push(stack, 'a) infix +> 40

  stack pop(stack)
  stack pop2(stack)
  stack pop3(stack)

  int topInt(stack)
  bool topBool(stack)
  ref topRef(stack)

  (* for convenience *)
  int topInt2(stack)
  int topInt3(stack)
  bool topBool2(stack)
  bool topBool3(stack)
  ref topRef2(stack)
  ref topRef3(stack)

(*
 * pop/push
 *
 * The rules are defined only partially assuming that only
 * well defined byte code will be translated and symbolically
 * executed
 *)

rule stack_pop3_push3
  find pop3(push(push(push(%st, %v1), %v2), %v3))
  replace %st
  tags rewrite "fol simp"

rule stack_pop2_push2
  find pop2(push(push(%st, %v1), %v2))
  replace %st
  tags rewrite "fol simp"

rule stack_pop_push
  find pop(push(%st, %v1))
  replace %st
  tags rewrite "fol simp"

(*
 * top/push
 *
 * The rules are defined only partially assuming that only
 * well defined byte code will be translated and symbolically
 * executed
 *)

rule stack_topInt_pushInt
  find topInt(push(%st, %v as int))
  replace %v
  tags rewrite "fol simp"

rule stack_topBool_pushBool
  find topBool(push(%st, %v as bool))
  replace %v
  tags rewrite "fol simp"

rule stack_topRef_pushRef
  find topRef(push(%st, %v as ref))
  replace %v
  tags rewrite "fol simp"

rule stack_topInt_pushBool
  find topInt(push(%st, %v as bool))
  replace cond(%v, 1, 0)
  tags rewrite "fol simp"

rule stack_topBool_pushInt
  find topBool(push(%st, %v as int))
  replace %v = 1
  tags rewrite "fol simp"

(*
 * top conveniences
 *)

rule stack_topInt2
  find topInt2(%st)
  replace topInt(pop(%st))

rule stack_topInt3
  find topInt3(%st)
  replace topInt(pop2(%st))

rule stack_topBool2
  find topBool2(%st)
  replace topBool(pop(%st))

rule stack_topBool3
  find topBool3(%st)
  replace topBool(pop2(%st))

rule stack_topRef2
  find topRef2(%st)
  replace topRef(pop(%st))

rule stack_topRef3
  find topRef3(%st)
  replace topRef(pop2(%st))