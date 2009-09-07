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
  array('type)
  location('type)
  locset

function
  'a defaultVal
  ref null
  array('a) nil


  location('a) loc(ref, field('a)) unique
  location('a) locA(array('a), int) unique
  location(int) length(array('a)) unique

  'a sel(heap, location('a))
  heap stor(heap, location('a), 'a)

  heap newObject(heap, ref)
  heap newArray(heap, array('a), int)
  heap havocHeap(heap, heap, locset)

  bool inLocset(location('a), locset)

rule heap_stor
  find sel(stor(%h, %loc1, %v), %loc2)
  replace cond(%loc1=%loc2, %v, sel(%h, locA(%r2, %i2)))
  tags rewrite "fol simp"

rule heap_newObject
  find sel(newObject(%h, %o), loc(%ref, %f))
  replace cond(%o = %ref, defaultVal, sel(%h, loc(%ref, %f)))

rule heap_newArray
  find sel(newArray(%h, %arr, %size), length(%arr2))
  replace cond(%arr = %arr2, %size, sel(%h, length(%arr2)))

rule heap_havocHeap
  find sel(havocHeap(%h, %h2, %locset), %loc)
  replace sel(cond(inLocset(%loc, %locset), %h2, %h1), %loc)

rule defaultVal_int
  find defaultVal as int
  replace 0

rule defaultVal_ref
  find defaultVal as ref
  replace null

rule defaultVal_array
  find defaultVal as array('a)
  replace nil

rule defaultVal_bool
  find defaultVal as bool
  replace false
