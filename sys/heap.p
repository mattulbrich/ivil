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
   "$set.p"

#plugin
#  prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.HeapPrettyPrinter"

sort
  ref
  field('type)
  heap as {'a}[ref,field('a)]'a

function
  'a defaultVal
  ref null

  field(int) idxInt(int) unique
  field(bool) idxBool(int) unique
  field(ref) idxRef(int) unique
  field(bool) created unique

  heap create(heap, ref)
  heap meld(heap, heap, set(ref))
  bool modHeap(heap, heap, set(ref))
  bool wellformed(heap)
  int arrlen(ref)

(* is this still there?!
rule heap_sel_stor_wrong_type
  find sel(stor(%h, %o, %f, %v), %o2, %f2)
  where differentTypes %f, %f2
  replace sel(%h, %o2, %f2)
  tags rewrite "fol simp"
       verbosity "5"
*)

(*
 * basic axioms in addition to the map type axioms
 *)

axiom positive_array_length
  (\forall r; arrlen(r) >= 0)

rule null_is_created
  find (%h as heap)[null, created]
  samegoal "..."
    replace true
  samegoal "show wellformedness"
    add |- wellformed(%h)
  tags
    asAxiom

rule heap_extensionality
  find %h1 = %h2 as heap
  where freshTypeVar type as %'a, %h1, %h2
  where freshVar %o as ref, %h1, %h2
  where freshVar %f as field(%'a), %h1, %h2
  replace (\T_all %'a; (\forall %o; (\forall %f;
            %h1[%o, %f] = %h2[%o, %f])))

(* TODO reformulate this using the lambda mechanism *)
rule heap_meld
  find meld(%h1, %h2, %set)[%o, %f]
  replace cond(%o ::  %set, %h1, %h2)[%o, %f]
  tags rewrite "fol simp"
       verbosity "5"
       asAxiom

rule heap_meld_eq
  assume meld(%h1, %h2, %set) = %t |-
  find %t[%o, %f]
  replace cond(%o ::  %set, %h1, %h2)[%o, %f]
  tags rewrite "fol simp"
       verbosity "5"

(* 
 * Wellformedness
 *)
rule wellformed_store
  find |- wellformed((%h as heap)[%o, %f := %ref])
  replace wellformed(%h) & %h[%ref, created]

rule wellformed_use
  assume wellformed(%h) |-
  find %h[%o, %f]
  add %h[%h[%o, %f], created] |-
  tags asAxiom

(*
 * newObject
 *)

rule heap_newObject
  find create(%h, %o)[%ref, %f]
  replace cond(%o = %ref, defaultVal, %h[%ref, %f])
  tags rewrite "fol simp"
       verbosity "5"

(*
 * modHeap
 * TODO prove the derived formulae
 *)

rule modHeap_definition
  find modHeap(%h, %h0, %set)
  where freshVar %x, %h, %h0, %set
  replace (\exists %x; %h = meld(%x, %h0, %set))

# to be proved!
rule modHeap_empty
  find modHeap(%h, %h2, emptyset)
  replace %h = %h2
  tags
    derived
    rewrite "concrete"

rule modHeap_left
  find modHeap(%h, %ho, %set) |-
  replace %h = meld($$skolem(%h), %ho, %set)
  tags
    derived
    rewrite "fol simp"

# proved correct!
rule modHeap_no_changes
  find modHeap(%h, %h, %set)
  replace true
  tags
    derived
    rewrite "concrete"

rule modHeap_right
  find |- modHeap(%h, %ho, %set)
  where freshTypeVar type as %'a, %h, %ho, %set
  where freshVar %o as ref, %h, %ho, %set
  where freshVar %f as field(%'a), %h, %ho, %set
  replace (\T_all %'a; (\forall %o; (\forall %f;
            %o :: %set | %h[%o, %f] = %ho[%o, %f])))
  tags
    derived
    rewrite "fol simp"

# rule heap_meld_eq
#   find %h = meld(%h1, %h2, %set)
#   replace cond(%o ::  %set, %h1, %h2)[%o, %f]

rule help_meld_eq_right
  find %h = meld(%h1, %h2, %set)
  where freshTypeVar arb as %'a, %h1, %h2, %set
  where freshVar %o as ref, %h1, %h2, %set
  where freshVar %f as field(%'a), %h1, %h2, %set
  replace (\T_all %'a; (\forall %o; (\forall %f;
            %h[%o, %f] = cond(%o ::  %set, %h1, %h2)[%o, %f])))

(*
 * default values
 *)

rule defaultVal_int
  find defaultVal as int
  replace 0
  tags rewrite "concrete"
       verbosity "5"

rule defaultVal_ref
  find defaultVal as ref
  replace null
  tags rewrite "concrete"
       verbosity "5"

rule defaultVal_bool
  find defaultVal as bool
  replace false
  tags rewrite "concrete"
       verbosity "5"

problem true
