 #
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains infrastructure for reference type treatment
 *)

include "$heap.p"

sort
  reftype

function 
  reftype array(reftype) unique
  reftype typeof(ref)
  bool subtype(reftype, reftype)
  bool interface(reftype)

  reftype array_int unique
  reftype array_char unique
  reftype array_short unique
  reftype array_boolean unique
  reftype array_long unique
  reftype array_byte unique

  reftype C_java_lang_Object unique
  reftype C_null unique

rule subtype_reflexive
  find subtype(%r, %r)
  replace true
  tags
    asAxiom
    rewrite "fol simp"

rule subtype_transitive
  assume subtype(%r, %s) |-
  find subtype(%s, %t) |-
  add subtype(%r, %t) |-
  tags
    rewrite "fol simp"

rule covariant_arraytypes
  find subtype(array(%t1), array(%t2))
  replace subtype(%t1, %t2)
  tags
    rewrite "fol simp"
    asAxiom

rule singleInheritance
  assume subtype(%a, %b) |-
  find subtype(%a, %c) |-
  add %a = C_null | subtype(%b,%c) | subtype(%c,%b) |
       interface(%b) | interface(%c) |-

axiom singleInheritance
  (\forall a; (\forall b; (\forall c;
    subtype(a, c)~~>
    subtype(a, b) & subtype(a, c) ->
    a = C_null | subtype(b,c) | subtype(c,b) |
       interface(b) | interface(c) )))
    
rule object_is_top
  find subtype(%x, C_java_lang_Object)
  replace true
  tags
    asAxiom
    rewrite "fol simp"

rule object_is_top2
  find subtype(C_java_lang_Object, %x)
  replace C_java_lang_Object = %x
  tags
    asAxiom
    rewrite "fol simp"

rule object_is_not_interface
  find interface(C_java_lang_Object)
  replace false
  tags
    asAxiom
    rewrite "concrete"

rule null_is_bottom
  find subtype(C_null, %x)
  replace true
  tags
    asAxiom
    rewrite "concrete"

rule null_has_one_element
  find typeof(%x) = C_null
  replace %x = null
  tags
    asAxiom
    rewrite "fol simp"

rule subtypes_array_int
  find subtype(%x, array_int)
  replace %x = array_int | %x = C_null
  tags 
    asAxiom
    rewrite "fol simp"

# todo same for the other primitive array types
