#
# This file is part of This file is part of
#    ibc - ivil bytecode compiler
#
# Copyright (C) 2011-2012 Universitaet Karlsruhe, Germany
#    written by Timm Felden
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#
(* jbc preamble, used by all java bytecode translations *)
 
 
(******************************************************************************)
(*********************************** BASE *************************************)
(******************************************************************************)
 (*
 This is the basic include file defines types and null, and names for types
 which are used to access some structures in a typesafe way.
*)

include
    "$base.p"
    "$decproc.p"
    "$fol.p"
    "$int.p"
    "$plugins.p"
    "$proposition.p"
    "$symbex.p"
    "$pair.p"
    "$set.p"
    "order.p"
    
properties
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,KnowledgeStrategy,SMTStrategy"
    
# float type for float and double
sort
 float

sort
 ref
 type
 field
 
sort
 heap as {'a}[ref, field]'a
 
 
(******************************************************************************)
(***************************** SPECIAL FUNCTIONS ******************************)
(******************************************************************************)
# special functions to model this, normal and exceptional termination values
 
# null is indeed a very special ref, as most statements about general refs dont hold for null
function ref $null

function 'a $result

function ref $exception assignable

# these temporary function symbols are used to create new objects. This is not necessary, but it increases the readability of Sequents
function
  ref $newObject assignable
  ref $newArray assignable
  int $javaMark assignable

function type T_java_lang_Object unique
function float FLOAT_0_0 unique

(******************************************************************************)
(*********************************** TYPES ************************************)
(******************************************************************************)

# typeof relation to determine the dynamic type of a reference
function bool instanceof(ref, type)

axiom type_of_null (\forall t; instanceof($null, t))

# any well typed object is also a T_java_lang_Object
axiom type_T_java_lang_Object_is_the_root_of_the_type_hierarchy (\forall o; instanceof(o, T_java_lang_Object))

function bool superType(type, type)

rule replace_superType_by_definition
find superType(%t, %s)
replace (\forall o; instanceof(o,%t) -> instanceof(o,%s))
tags rewrite "concrete"

# denote that the type of a created Object is exactly a certain type
function type exactTypeOf(ref)

# if an object has the exact type t, it has no subtype of t
axiom exactTypeOf_lower_bound (\forall t; (\forall o; (\forall s; t = exactTypeOf(o) & superType(s,t) & instanceof(o,s) -> s=t)))

# if an object has exactly the type t, it is also a type of t
axiom exactTypeOf_is_also_instanceof (\forall o; (\forall t; t = exactTypeOf(o) -> instanceof(o,t)))

# the invariant predicate tells if an object fulfills its invariants on a given heap for a given type
function bool $invariant(heap, ref, type)


# the wildcard binder is used to create wildcard types; the axioms for these types are created by the surrounding generic type as needed
binder type (\wildcard type; bool)

# allow interactive replacement of wildcards
rule wildcard_to_existential_type_interactive
find (\wildcard %w; %phi)
add $$subst(%w, $$skolem(%w), %phi) |-
replace $$skolem(%w)

(******************************************************************************)
(********************************** ARRAYS ************************************)
(******************************************************************************)

# create a field for each index into the array
function field $array_index(int) unique

# length of arrays is stored in a field as well
function field $array_length unique

# length of arrays can not be negative
# note: it is not important if an object is actually an array, the length wont be accessed in that case anyway
axiom array_length_is_positive_on_wellformed_heaps (\forall h; (\forall o; $wellformed(h) -> h[o,$array_length] >= 0))

# type constructor for arrays
function type TF_array(type) unique

# the array typeing axiom
axiom type_arrays_are_special_types (\forall o; (\forall t; (\forall s; (instanceof(o,t) & instanceof(o,TF_array(s))) -> (t=T_java_lang_Object | (\exists s_; t=TF_array(s_) & superType(s_, s))))))


(*
 special types needed for base type arrays 
 
 the subtypeing is done by the translator on demand
*)
function type TF_BOOLEAN_ARRAY unique
function type TF_CHAR_ARRAY unique
function type TF_FLOAT_ARRAY unique
function type TF_DOUBLE_ARRAY unique
function type TF_BYTE_ARRAY unique
function type TF_SHORT_ARRAY unique
function type TF_INT_ARRAY unique
function type TF_LONG_ARRAY unique


(******************************************************************************)
(*********************************** HEAP *************************************)
(******************************************************************************)

function heap $heap assignable
function heap $old_heap assignable
# temporary heap is used to save the old heap during calls
function heap $temporary_heap assignable
# delta heap is used by multianewarray
function heap $delta_heap assignable

# (frame, heap, heap) -> heap := λframe,h,h'.cond(<o,f> :: frame, h, h')
binder heap (\merge_heap prod(ref, field); bool; heap; heap)
rule heap_load_merge
  find $load_heap((\merge_heap %p; %phi; %h1; %h2), %o, %f)
  replace $load_heap(cond($$subst(%p, pair(%o, %f), %phi), %h1, %h2), %o, %f)
  tags rewrite "concrete"
  
rule heap_merge_eq_to_cond
  find %h = (\merge_heap %p; %phi; %h1; %h2)
  where toplevel
  replace (\forall o; (\forall f; cond($$subst(%p, pair(o, f), %phi), %h = %h1, %h = %h2)))
  tags rewrite "split"
  
rule heap_merge_eq_to_cond2
  find (\merge_heap %p; %phi; %h1; %h2) = %h
  where toplevel
  replace (\forall o; (\forall f; cond($$subst(%p, pair(o, f), %phi), %h = %h1, %h = %h2)))
  tags rewrite "split"


# static is a magic referenec which models all static field
function ref $static
# to make things easier, static uses the same interpretation as null
axiom static_is_null $static = $null

# the created field is used to distinguish between used and unused heap space
function field $created unique

# this function symbol is used to sort out unreachable heap states
function bool $wellformed(heap)

# null is created(because static is)
axiom heap_null_is_created (\forall h as heap; $wellformed(h) -> h[$null, $created])

# there are no nonnull fields which are not created (this is needed to know, that new objects are not yet used as fields)
axiom heap_field_is_created (\forall h as heap; (\forall o; (\forall f; $wellformed(h) -> h[h[o, f], $created])))
axiom heap_field_of_free_parents_are_null (\forall h as heap; (\forall o; (\forall f; $wellformed(h) -> (h[o, $created] | h[o,f] = $null))))

# alloc is similar to calloc, i.e. it creates a new object on a heap and initializes it with default values (zeroes)
function heap $alloc(ref, heap)

# read from allocation
rule heap_load_alloc_int
find $load_heap($alloc(%r, %h), %o, %f) as int
replace cond(%r = %o, 0, $load_heap(%h, %o, %f))
tags rewrite "fol simp"

rule heap_load_alloc_ref
find $load_heap($alloc(%r, %h), %o, %f) as ref
replace cond(%r = %o, $null, $load_heap(%h, %o, %f))
tags rewrite "fol simp"

rule heap_load_alloc_float
find $load_heap($alloc(%r, %h), %o, %f) as float
replace cond(%r = %o, FLOAT_0_0, $load_heap(%h, %o, %f))
tags rewrite "fol simp"

rule heap_load_alloc_bool
find $load_heap($alloc(%r, %h), %o, %f) as bool
replace cond(%r = %o, cond($created = %f, true, false), $load_heap(%h, %o, %f))
tags rewrite "fol simp"

rule heap_load_alloc_other
find $load_heap($alloc(%r, %h), %o, %f)
assume |- %r = %o
replace $load_heap(%h, %o, %f)
tags rewrite "concrete"

rule heap_load_alloc_other2
find $load_heap($alloc(%r, %h), %o, %f)
assume |- %o = %r
replace $load_heap(%h, %o, %f)
tags rewrite "concrete"

# this axiom is used to allow z3 to close goals whith new objects on them
axiom heap_load_alloc_simplification (\forall h; (\forall o; (\forall f; (\forall n; (\T_all 'a; $alloc(n, h)[o, f] as 'a  = h[o, f]) | o = n))))


# TODO: rename this binder
# the translation of multianewarray requires a binder \newobject, that creates a new object on one heap, that is not present on another while mainting wellformedness
binder ref (\newObject ref; bool; heap; heap)

# TODO: this rule musst not be used inside another binder
rule newObject_simplification
  find  (\newObject %x; %b; %oldh; %h)
  add $wellformed(%oldh) -> (!%oldh[$$subst(%x, $$skolem(%x), %x), $created] & %h[$$subst(%x, $$skolem(%x), %x), $created] & $$subst(%x, $$skolem(%x), %b)) |-
  replace  $$subst(%x, $$skolem(%x), %x)
  tags rewrite "fol simp"


(******************************************************************************)
(******************************** ARITHMETIC **********************************)
(******************************************************************************)

(*  bool  *)

function
  bool $neq('a, 'a)
  bool $explies(bool, bool) infix <-  20
  #actually, explies is used by JML and not by ibc
  
(*  int  *)
function
  int $iadd(int, int)
  int $idiv(int, int)
  int $imul(int, int)
  int $irem(int, int)
  int $ishl(int, int)
  int $ishr(int, int)
  int $isub(int, int)
  int $iushr(int, int)
  'a $iand('a, 'a)
  'a $ior('a, 'a)
  'a $ixor('a, 'a)
 #' (fix for pascal syntax highlighting :-/)
  
  int $ineg(int)
  int $iinverse(int)
  
  
  int $ladd(int, int)
  int $ldiv(int, int)
  int $lmul(int, int)
  int $lrem(int, int)
  int $lshl(int, int)
  int $lshr(int, int)
  int $lsub(int, int)
  int $lushr(int, int)
  int $land(int, int)
  int $lor(int, int)
  int $lxor(int, int)
  
  int $lneg(int)
  int $lcmp(int, int)
  
(* float *)
function
  float $fadd(float, float)
  float $fdiv(float, float)
  float $fmul(float, float)
  float $frem(float, float)
  float $fsub(float, float)
  float $dadd(float, float)
  float $ddiv(float, float)
  float $dmul(float, float)
  float $drem(float, float)
  float $dsub(float, float)
  
  float $fneg(float)
  float $dneg(float)
  
  int $fcmpl(float, float)
  int $fcmpg(float, float)
  int $dcmpl(float, float)
  int $dcmpg(float, float)
  
(* base type cast functions *)
function
  int $i2b(int)
  float $i2d(int)
  float $i2f(int)
  int $i2l(int)
  int $i2s(int)
  
  float $l2d(int)
  int $l2i(int)
  float $l2f(int)
  
  float $d2f(float)
  int $d2i(float)
  int $d2l(float)
  
  float $f2d(float)
  int $f2i(float)
  int $f2l(float)


(******************************************************************************)
(**************************** DEFAULT ARITHMETIC ******************************)
(******************************************************************************)

(* bool rules *)
rule neq_to_not_eq
find $neq(%a, %b)
replace $not($eq(%a, %b))
tags rewrite "concrete"

rule explies_to_implies
find $explies(%a, %b)
replace %b -> %a
tags rewrite "concrete"

rule iand_to_and
find $iand(%a, %b)
replace %a & %b
tags rewrite "concrete"

rule ior_to_or
find $ior(%a, %b)
replace %a | %b
tags rewrite "concrete"
(*
rule ixor_to_xor
find $ixor(%a, %b)
replace %a & %b
tags rewrite "concrete"
*)


(* int rules *)
 
rule iadd_to_plus
find $iadd(%a, %b)
replace $plus(%a, %b)
tags rewrite "concrete"

rule idiv_to_div
find $idiv(%a, %b)
replace $div(%a, %b)
tags rewrite "concrete"
 
rule imul_to_mult
find $imul(%a, %b)
replace $mult(%a, %b)
tags rewrite "concrete"

rule irem_to_definition
find $irem(%a, %b)
replace $isub(%a, $imul($idiv(%a, %b), %b))
tags rewrite "concrete"
 
rule ishl_to_shl
find $ishl(%a, %b)
replace $shl(%a, %b)
tags rewrite "concrete"
 
rule ishr_to_shr
find $ishr(%a, %b)
replace $shr(%a, %b)
tags rewrite "concrete"

rule isub_to_minus
find $isub(%a, %b)
replace $minus(%a, %b)
tags rewrite "concrete"
 
rule iushr_to_ushr
find $iushr(%a, %b)
replace $ushr(%a, %b)
tags rewrite "concrete"

rule ineg_to_neg
find $ineg(%a)
replace $neg(%a)
tags rewrite "concrete"

# jvmspec on ineg:  -x == ~x + 1; this equality is based on the fact, that any integer is stored as twos complement
rule iinverse_jvmstyle
find $iinverse(%a)
replace $neg(%a) - 1
tags rewrite "concrete"

(* long *)

rule ladd_to_plus
find $ladd(%a, %b)
replace $plus(%a, %b)
tags rewrite "concrete"

(*
rule land_to_?
find $land(%a, %b)
replace $plus(%a, %b)
tags rewrite "concrete"
 *)
rule ldiv_to_div
find $ldiv(%a, %b)
replace $div(%a, %b)
tags rewrite "concrete"
 
rule lmul_to_mult
find $lmul(%a, %b)
replace $mult(%a, %b)
tags rewrite "concrete"
 (*
rule lor_to_?
find $lor(%a, %b)
replace $mult(%a, %b)
tags rewrite "concrete"
 *)
rule lrem_to_definition
find $lrem(%a, %b)
replace $lsub(%a, $lmul($ldiv(%a, %b), %b))
tags rewrite "concrete"
 
rule lshl_to_shl
find $lshl(%a, %b)
replace $shl(%a, %b)
tags rewrite "concrete"
 
rule lshr_to_shr
find $lshr(%a, %b)
replace $shr(%a, %b)
tags rewrite "concrete"

rule lsub_to_minus
find $lsub(%a, %b)
replace $minus(%a, %b)
tags rewrite "concrete"
 
rule lushr_to_ushr
find $lushr(%a, %b)
replace $ushr(%a, %b)
tags rewrite "concrete"
 (*
rule lxor_to_?
find $lxor(%a, %b)
replace $minus(%a, %b)
tags rewrite "concrete"
*)

rule lneg_to_neg
find $lneg(%a)
replace $neg(%a)
tags rewrite "concrete"

rule lcmp_to_cond
find $lcmp(%a, %b)
replace cond(%a = %b, 0, cond(%a < %b, -1, 1))
tags rewrite "concrete"

#note this is an oversimplification
rule fcmpl_to_cond
find $fcmpl(%a, %b)
replace cond(%a = %b, 0, cond($order_less(%a, %b), -1, 1))
tags rewrite "concrete"

#note this is an oversimplification
rule fcmpg_to_cond
find $fcmpg(%a, %b)
replace cond(%a = %b, 0, cond($order_less(%a, %b), -1, 1))
tags rewrite "concrete"

#note this is an oversimplification
rule dcmpl_to_cond
find $dcmpl(%a, %b)
replace cond(%a = %b, 0, cond($order_less(%a, %b), -1, 1))
tags rewrite "concrete"

#note this is an oversimplification
rule dcmpg_to_cond
find $dcmpg(%a, %b)
replace cond(%a = %b, 0, cond($order_less(%a, %b), -1, 1))
tags rewrite "concrete"

# note: ignore rules are not entirely correct. there should maybe be something like cond(%a > maxVal, -%a, %a)

rule i2s_ignore
find $i2s(%a)
replace %a
tags rewrite "concrete"

rule i2b_ignore
find $i2b(%a)
replace %a
tags rewrite "concrete"

rule i2l_ignore
find $i2l(%a)
replace %a
tags rewrite "concrete"

rule l2i_ignore
find $l2i(%a)
replace %a
tags rewrite "concrete"


(******************************************************************************)
(*********************************** END **************************************)
(******************************************************************************)
