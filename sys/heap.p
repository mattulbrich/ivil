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

#plugin
#  prettyPrinter : "de.uka.iti.pseudo."

sort
  field('type)
  heap
  ref

  locset

function
  'a defaultVal
  ref null

  locset loc(ref, field('a)) unique

  field(int) intIdx(int) unique
  field(bool) boolIdx(int) unique
  field(ref) refIdx(int) unique

  'a sel(heap, ref, field('a))
  heap stor(heap, ref, field('a), 'a)

  heap newObject(heap, ref)
  heap disturb(heap, heap, locset)

function
  bool inLocset(ref, field('a), locset)

  locset everything
  locset nothing

  locset $union(locset, locset) infix :: 40

binder
  locset (\union 'a; bool; locset)

(*
 * stor
 *)

rule heap_sel_stor
  find sel(stor(%h, %o, %f, %v), %o2, %f2)
  replace cond(%loc1=%loc2 & %f=%f2, %v, sel(%h, %o, %f))
  tags rewrite "fol simp"

(*
 * newObject
 *)

rule heap_newObject
  find sel(newObject(%h, %o), %ref, %f)
  replace cond(%o = %ref, defaultVal, sel(%h, %ref, %f))
  tags rewrite "fol simp"

(*
 * disturb
 *)

rule heap_havocHeap
  find sel(disturb(%h1, %h2, %locset), %o, %f)
  replace sel(cond(inLocset(%o, %f, %locset), %h2, %h1), %o, %f)
  tags rewrite "fol simp"

(*
 * default values
 *)

rule defaultVal_int
  find defaultVal as int
  replace 0
  tags rewrite "fol simp"

rule defaultVal_ref
  find defaultVal as ref
  replace null
  tags rewrite "fol simp"

rule defaultVal_bool
  find defaultVal as bool
  replace false
  tags rewrite "fol simp"

(*
 * locset
 *)

(* a location is in its own singleton *)
rule locset_self
  find inLocset(%o, %f, loc(%o2, %f2))
  replace %o=%o2 & %f=%f2
  tags rewrite "fol simp"

(* a location is in a union if in own of the two 
 * operands *)
rule locset_union
  find inLocset(%o, %f, %s1::%s2)
  replace inLocset(%o, %f, %s1) | inLocset(%o, %f, %s2)
  tags rewrite "fol simp"

(* a location is in a set extension iff there is
 * one instantiation such that the location is
 * in it *)
rule locset_extension
  find inLocset(%o, %f, (\union %x; %g; %s))
  replace (\exists %x; %g & inLocset(%o, %f, %s))
  tags rewrite "fol simp"

(* any location is in everything *)
rule locset_everything
  find inLocset(%o, %f, everything)
  replace true
  tags rewrite "fol simp"

(* no location is in nothing *)
rule locset_nothing
  find inLocset(%o, %f, nothing)
  replace false
  tags rewrite "fol simp"