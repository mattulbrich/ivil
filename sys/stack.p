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

sort
  stack
  depth

function
  depth ZERO
  depth D(depth)

  stack empty
  stack push(stack, 'a)

  stack pop(stack)
  stack pop2(stack)
  stack pop3(stack)
  stack popN(depth, stack)

  'a top(stack)
  'a top2(stack)
  'a top3(stack)
  'a topN(depth, stack)

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

rule stack_pop0
  find popN(ZERO, %st)
  replace %st
  tags rewrite "fol simp"

rule stack_popN_push
  find popN(D(%d), push(%st, %v))
  replace popN(%d, %st)
  tags rewrite "fol simp"

(*
 * top/push
 *
 * The rules are defined only partially assuming that only
 * well defined byte code will be translated and symbolically
 * executed
 *)

rule stack_top3_push3
  find top(push(push(push(%st, %v1 as 'a), %v2), %v3)) as 'a
  replace %v1 as 'a
  tags rewrite "fol simp"

rule stack_top2_push2
  find top(push(push(%st, %v1 as 'a), %v2)) as 'a
  replace %v1 as 'a
  tags rewrite "fol simp"

rule stack_top_push
  find top(push(%st, %v1 as 'a)) as 'a
  replace %v1 as 'a
  tags rewrite "fol simp"

rule stack_topN_DZERO
  find topN(D(ZERO), push(%st, %v as 'a)) as 'a
  replace %v as 'a
  tags rewrite "fol simp"

rule stack_topN_D
  find topN(D(%d), push(%st, %v))
  replace topN(%d, %st)
  tags rewrite "fol simp"