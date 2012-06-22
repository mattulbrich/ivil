(* created by JBC - Mon Jun 11 13:46:17 CEST 2012 *)

include "jbc_preamble.p"
(* the proof obligation starts here: *)


rule type_invariant_T_java_lang_Object
  find $invariant(%h, %o, T_java_lang_Object)
  replace true
  tags rewrite "fol simp"
function type T_First unique
axiom type__T_First__extends__T_java_lang_Object superType(T_First, T_java_lang_Object)


rule type_invariant_T_First
  find $invariant(%h, %o, T_First)
  replace true
  tags rewrite "fol simp"
function ref R_this_ref assignable
function int R_n_int assignable
function int R_sum_int assignable
function int R_i_int assignable
function int $stack_1_int assignable
function heap $SPEC_loop_heap assignable
function set(prod(ref, field)) $SPEC_loop_frame assignable
function int $stack_2_int assignable


program Java
 source "file:First.jml"
  assume $wellformed($heap)
  $exception := $null
  $old_heap := $heap
  assume !R_this_ref = $null & $heap[R_this_ref, $created]
  assume instanceof(R_this_ref, T_First)
  
sourceline 8  PC0:
  # iconst_0
  $stack_1_int := 0
  
sourceline 8  PC1:
  # istore_2
  R_sum_int := $stack_1_int
  
sourceline 10  PC2:
  havoc $SPEC_loop_frame
  assume (\forall o; (\forall f; (pair(o, f) :: $SPEC_loop_frame) = (false)))
  $SPEC_loop_heap:= $heap
  
sourceline 12  PC7:
  # iconst_1
  $stack_1_int := 1
  
sourceline 12  PC8:
  # istore_3
  R_i_int := $stack_1_int
  
sourceline 12  PC9:
  assume $wellformed($heap)
  skip LOOPINV, $heap = (\merge_heap p; p :: $SPEC_loop_frame | !$SPEC_loop_heap[fst(p), $created]; $heap; $SPEC_loop_heap) & (\forall o; $SPEC_loop_heap[o,$created] -> $heap[o,$created]) & $wellformed($heap) & true
  assume $wellformed($heap)
  # iload_3
  $stack_1_int := R_i_int
  
sourceline 12  PC10:
  # iload_1
  $stack_2_int := R_n_int
  
sourceline 12  PC11:
  # if_icmpgt
  goto PC11branch, PC11cont
  PC11branch: assume $gt($stack_1_int, $stack_2_int)
  goto PC29
  PC11cont: assume !$gt($stack_1_int, $stack_2_int)
  goto PC14
  
sourceline 13  PC14:
  $javaMark := 1 skip
  
sourceline 14  PC19:
  # iload_2
  $stack_1_int := R_sum_int
  
sourceline 14  PC20:
  # iload_3
  $stack_2_int := R_i_int
  
sourceline 14  PC21:
  # iadd
  $stack_1_int := $iadd($stack_1_int, $stack_2_int)
  
sourceline 14  PC22:
  # istore_2
  R_sum_int := $stack_1_int
  
sourceline 12  PC23:
  # iinc
  R_i_int := $iadd(R_i_int, 1)
  
sourceline 12  PC26:
  # goto
  goto PC9
  
sourceline 17  PC29:
  # iload_2
  $stack_1_int := R_sum_int
  
sourceline 17  PC30:
  # ireturn
  assume $result = $stack_1_int
  end
function bool $SPEC_diverges assignable
function set(prod(ref, field)) $SPEC_frame
function ref SPEC_pre_R_this_ref assignable
function int SPEC_pre_R_n_int assignable

problem R_this_ref = SPEC_pre_R_this_ref & R_n_int = SPEC_pre_R_n_int & (\forall o; (\forall f; (pair(o, f) :: $SPEC_frame) = (false))) & $invariant($heap, R_this_ref, T_java_lang_Object) & $invariant($heap, R_this_ref, T_First) &  $SPEC_diverges = (true) & true -> $SPEC_diverges | { R_this_ref := SPEC_pre_R_this_ref || R_n_int := SPEC_pre_R_n_int}[[0;Java]](true & $invariant($heap, R_this_ref, T_java_lang_Object) & $invariant($heap, R_this_ref, T_First) & ( \forall o as ref; ( \forall f as field; pair(o, f) :: $SPEC_frame | !$old_heap[o, $created] | ($heap[o,f] = $old_heap[o,f] as int & ($heap[o,f] = $old_heap[o,f] as bool) & $heap[o,f] = $old_heap[o,f] as ref & $heap[o,f] = $old_heap[o,f] as float))))