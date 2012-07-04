(* created by JBC - Wed Jul 04 17:47:55 CEST 2012 *)

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
function ref this assignable
function ref array assignable
function int result assignable
function int i assignable
function int $stack_1_int assignable
function ref $stack_2_ref assignable
function int $stack_2_int assignable
function int $stack_3_int assignable


program Java
 source "file:Sum.jml"
  assume $wellformed($heap)
  $exception := $null
  $old_heap := $heap
  assume !this = $null & $heap[this, $created]
  assume instanceof(this, T_Sum)
  assume $heap[array, $created]
  assume instanceof(array, TF_INT_ARRAY)
  
sourceline 7  PC0:
  # iconst_0
  $stack_1_int := 0
  
sourceline 7  PC1:
  # istore_2
  result := $stack_1_int
  
sourceline 8  PC2:
  # iconst_0
  $stack_1_int := 0
  
sourceline 8  PC3:
  # istore_3
  i := $stack_1_int
  
sourceline 9  PC4:
  assume $wellformed($heap)
  # iload_3
  $stack_1_int := i
  
sourceline 9  PC5:
  # aload_1
  $stack_2_ref := array
  
sourceline 9  PC6:
  # arraylength
  assert !$null = $stack_2_ref; "no implicit NullPointerException"
  $stack_2_int := $heap[$stack_2_ref, $array_length]
  
sourceline 9  PC7:
  # if_icmpge
  goto PC7branch, PC7cont
  PC7branch: assume $gte($stack_1_int, $stack_2_int)
  goto PC32
  PC7cont: assume !$gte($stack_1_int, $stack_2_int)
  goto PC10
  
sourceline 10  PC10:
  skip MARK, 1
  
sourceline 11  PC15:
  # iload_2
  $stack_1_int := result
  
sourceline 11  PC16:
  # aload_1
  $stack_2_ref := array
  
sourceline 11  PC17:
  # iload_3
  $stack_3_int := i
  
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
  result := $stack_1_int
  
sourceline 12  PC21:
  # iinc
  i := $iadd(i, 1)
  
sourceline 13  PC24:
  skip MARK, 2
  
sourceline 13  PC29:
  # goto
  goto PC4
  
sourceline 15  PC32:
  # iload_2
  $stack_1_int := result
  
sourceline 15  PC33:
  # ireturn
  assume $result = $stack_1_int
  end
function bool $SPEC_diverges assignable
function set(prod(ref, field)) $SPEC_frame
function ref SPEC_pre_this assignable
function ref SPEC_pre_array assignable

problem this = SPEC_pre_this & array = SPEC_pre_array & (\forall o; (\forall f; (pair(o, f) :: $SPEC_frame) = (false))) & $invariant($heap, this, T_Sum) & $invariant($heap, this, T_java_lang_Object) & !$eq(SPEC_pre_array, $null) & !$eq(SPEC_pre_array, $null) -> $SPEC_diverges | { this := SPEC_pre_this || array := SPEC_pre_array}[[0;Java]](true & $invariant($heap, this, T_Sum) & $invariant($heap, this, T_java_lang_Object) & ( \forall o as ref; ( \forall f as field; pair(o, f) :: $SPEC_frame | !$old_heap[o, $created] | ($heap[o,f] = $old_heap[o,f] as int & ($heap[o,f] = $old_heap[o,f] as bool) & $heap[o,f] = $old_heap[o,f] as ref & $heap[o,f] = $old_heap[o,f] as float))))