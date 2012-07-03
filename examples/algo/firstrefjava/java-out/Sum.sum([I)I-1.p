(* created by JBC - Tue Jul 03 17:45:51 CEST 2012 *)

include "jbc_preamble.p"
(* the proof obligation starts here: *)


rule type_invariant_T_java_lang_Object
  find $invariant(%h, %o, T_java_lang_Object)
  replace true
  tags rewrite "fol simp"
function type T_Sum unique
axiom type__T_Sum__extends__T_java_lang_Object superType(T_Sum, T_java_lang_Object)


rule type_invariant_T_Sum
  find $invariant(%h, %o, T_Sum)
  replace true
  tags rewrite "fol simp"
function ref R_this_ref assignable
function ref R_array_ref assignable
function int R_result_int assignable
function int R_i_int assignable
function int $stack_1_int assignable
function ref $stack_2_ref assignable
function int $stack_2_int assignable
function int $stack_3_int assignable


program Java
 source "file:Sum.jml"
  assume $wellformed($heap)
  $exception := $null
  $old_heap := $heap
  assume !R_this_ref = $null & $heap[R_this_ref, $created]
  assume instanceof(R_this_ref, T_Sum)
  assume $heap[R_array_ref, $created]
  assume instanceof(R_array_ref, TF_INT_ARRAY)
  
sourceline 7  PC0:
  # iconst_0
  $stack_1_int := 0
  
sourceline 7  PC1:
  # istore_2
  R_result_int := $stack_1_int
  
sourceline 8  PC2:
  # iconst_0
  $stack_1_int := 0
  
sourceline 8  PC3:
  # istore_3
  R_i_int := $stack_1_int
  
sourceline 9  PC4:
  assume $wellformed($heap)
  # iload_3
  $stack_1_int := R_i_int
  
sourceline 9  PC5:
  # aload_1
  $stack_2_ref := R_array_ref
  
sourceline 9  PC6:
  # arraylength
  assert !$null = $stack_2_ref; "no implicit NullPointerException"
  $stack_2_int := $heap[$stack_2_ref, $array_length]
  
sourceline 9  PC7:
  # if_icmpge
  goto PC7branch, PC7cont
  PC7branch: assume $gte($stack_1_int, $stack_2_int)
  goto PC27
  PC7cont: assume !$gte($stack_1_int, $stack_2_int)
  goto PC10
  
sourceline 10  PC10:
  skip MARK, 1
  
sourceline 11  PC15:
  # iload_2
  $stack_1_int := R_result_int
  
sourceline 11  PC16:
  # aload_1
  $stack_2_ref := R_array_ref
  
sourceline 11  PC17:
  # iload_3
  $stack_3_int := R_i_int
  
sourceline 11  PC18:
  # iaload
  assert 0 <= $stack_3_int & $stack_3_int < $heap[$stack_2_ref, $array_length]; "no implicit ArrayOutOfBoundsException"
  assert !$null=$stack_2_ref; "no implicit NullPointerException"
  $stack_2_int := $heap[$stack_2_ref, $array_index($stack_3_int)]
  
sourceline 11  PC19:
  # iadd
  $stack_1_int := $iadd($stack_1_int, $stack_2_int)
  
sourceline 11  PC20:
  # istore_2
  R_result_int := $stack_1_int
  
sourceline 12  PC21:
  # iinc
  R_i_int := $iadd(R_i_int, 1)
  
sourceline 12  PC24:
  # goto
  goto PC4
  
sourceline 14  PC27:
  # iload_2
  $stack_1_int := R_result_int
  
sourceline 14  PC28:
  # ireturn
  assume $result = $stack_1_int
  end
function bool $SPEC_diverges assignable
function set(prod(ref, field)) $SPEC_frame
function ref SPEC_pre_R_this_ref assignable
function ref SPEC_pre_R_array_ref assignable

problem R_this_ref = SPEC_pre_R_this_ref & R_array_ref = SPEC_pre_R_array_ref & (\forall o; (\forall f; (pair(o, f) :: $SPEC_frame) = (false))) & $invariant($heap, R_this_ref, T_Sum) & $invariant($heap, R_this_ref, T_java_lang_Object) & !$eq(SPEC_pre_R_array_ref, $null) & !$eq(SPEC_pre_R_array_ref, $null) -> $SPEC_diverges | { R_this_ref := SPEC_pre_R_this_ref || R_array_ref := SPEC_pre_R_array_ref}[[0;Java]](true & $invariant($heap, R_this_ref, T_Sum) & $invariant($heap, R_this_ref, T_java_lang_Object) & ( \forall o as ref; ( \forall f as field; pair(o, f) :: $SPEC_frame | !$old_heap[o, $created] | ($heap[o,f] = $old_heap[o,f] as int & ($heap[o,f] = $old_heap[o,f] as bool) & $heap[o,f] = $old_heap[o,f] as ref & $heap[o,f] = $old_heap[o,f] as float))))