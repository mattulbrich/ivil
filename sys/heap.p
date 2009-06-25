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
  heap
  ref
  locset

function
  'a defaultVal
  field(bool) $created
  field(int) $length
  ref null

  field(ref) arrayIndex(int)

  heap H assignable

  'a R(heap, ref, field('a))
  heap W(heap, ref, field('a), 'a)
  heap newObject(heap, ref)
  heap havocHeap(heap, heap, locset)

  bool inLocset(ref, field('a), locset)


rule heap_RW_same
  find R(W(%h, %o, %f as 'field, %v), %o2, %f2 as 'field)
  replace cond(%o = %o2 & %f = %f2, %v, R(%h, %o2, %f2))
  tags rewrite "fol simp"

rule heap_RW_diff
  find R(W(%h, %o, %f, %v), %o2, %f2)
  where
    differentTypes %f, %f2
  replace R(%h, %o2, %f2)
  tags rewrite "fol simp"


rule heap_Rnew
  find R(newObject(%h, %o), %o2, %f2)
  replace cond(%o = %o2, defaultVal, R(%h, %o2, %f2))

rule heap_Rhavoc
  find R(havocHeap(%h, %h2, %locset), %o, %f)
  replace R(cond(inLocset(%o, %f, %locset), %h2, %h1), %o, %f)



rule defaultVal_int
  find defaultVal as int
  replace 0

rule defaultVal_ref
  find defaultVal as ref
  replace null

rule defaultVal_bool
  find defaultVal as bool
  replace false