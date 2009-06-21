#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains infrastructure for heap manipulation.
 *)

include
   "$fol.p"

sort
  field('type)
  array('type)
  heap
  ref
  locset

function
  'a defaultVal
  field(bool) $created
  ref null

  heap H assignable
  'a R(heap, ref, field('a))
  heap W(heap, ref, field('a), 'a)
  heap N(heap, ref)
  heap WH(heap, heap, locset)

  'a Ra(heap, array('a), int)
  int La(heap, array('a))
  heap Wa(heap, array('a), int, 'a)
  heap Na(heap, array('a), int)

  bool inLocset(ref, field('a), locset)
  bool inLocsetA(array('a), int, locset)

rule heap_RW
  find R(W(%h, %o, %f, %v), %o2, %f2)
  replace cond(%o = %o2 & %f = %f2, %v, R(%h, %o2, %f2))
  tags rewrite "fol simp"

rule heap_RWa
  find R(Wa(%h, %a, %i, %v), %o2, %f2)
  replace R(%h, %o2, %f2)

rule heap_RN
  find R(N(%h, %o), %o2, %f2)
  replace cond(%o = %o2, defaultVal, R(%h, %o2, %f2))

rule defaultVal_int
  find defaultVal as int
  replace 0

rule defaultVal_ref
  find defaultVal as ref
  replace null

rule defaultVal_bool
  find defaultVal as bool
  replace false