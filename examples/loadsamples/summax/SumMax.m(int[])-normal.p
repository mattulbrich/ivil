(* created : Tue Feb 19 15:23:45 CET 2013 - Alpha-3 *)
#
# This file was generated by PSEUDO
# which is copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#

include "$bytecode.p"

include "declarations.p"
include "aha.p"

(* program variables *)
function
   ref _a assignable (* local variable '1' *)
   ref pre_a assignable (* method parameter, prestate *)
   set(ref) modSet assignable (* the assignable/modifies clause evaluated in the pre-state *)
   bool st_0_bool assignable (* stack variable *)
   int st_0_int assignable (* stack variable *)
   int _i assignable (* local variable '2' *)
   ref st_0_ref assignable (* stack variable *)
   bool st_1_bool assignable (* stack variable *)
   int st_1_int assignable (* stack variable *)
   heap ho1 assignable (* Heap snapshot for loop invariant *)
   ref st_1_ref assignable (* stack variable *)
   bool branchCond assignable (* branching condition *)
   int st_2_int assignable (* stack variable *)
   ref st_2_ref assignable (* stack variable *)
   int st_3_int assignable (* stack variable *)

(* the translated Java program *)
program Java source "./SumMax.jspec"
   sourceline 9
    assume !_a = null & arrlen(_a) = N ; "Assume precondition"
   sourceline 10
    assume (\forall k; 0 <= k & k < N -> 0 <= h[_a, idxInt(k)]) ; "Assume precondition"
    assume wellformed(h) ; "assumption guaranteed by Java"
    assume !_this = null ; "assumption guaranteed by Java"
    assume h[_this, created] ; "assumption guaranteed by Java"
    assume typeof(_this) = C_SumMax ; "assumption of the runtime type"
    assume subtype(typeof(_a), array_int) ; "Typing of argument"
    pre_h := h || pre_a := _a ; "Recording the prestate"
    decrBase := 0 ; "Save the decrease (variant) base"
    modSet := singleton(_this)
  Label1:
   sourceline 23
    (* ICONST_0 *)
    st_0_bool := false
    st_0_int := 0
    (* ISTORE *)
    _i := st_0_int
  Label2:
   sourceline 24
    (* ALOAD *)
    st_0_ref := _this
    (* ICONST_0 *)
    st_1_bool := false
    st_1_int := 0
    (* PUTFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    assert st_0_ref :: modSet ; "object is assignable"
    h := h[st_0_ref, F_SumMax_sum:= st_1_int]
  Label3:
   sourceline 25
    (* ALOAD *)
    st_0_ref := _this
    (* ICONST_0 *)
    st_1_bool := false
    st_1_int := 0
    (* PUTFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    assert st_0_ref :: modSet ; "object is assignable"
    h := h[st_0_ref, F_SumMax_max:= st_1_int]
  Label4:
   sourceline 27
    (* LDC + INVOKESTATIC - Specification injection *)
    ho1 := h
    (* spec statement deferred till after looping point *)
  Label5:
    (* deferred specification statement *)
    skip LOOPINV, {ho := ho1}((0 <= _i & _i<=N & 
	     (\forall k; 0 <= k & k < _i -> h[_a, idxInt(k)] <= h[_this, F_SumMax_max]) &
	     (_i = 0 -> h[_this, F_SumMax_max] = 0) &
	     (_i > 0 -> (\exists k; 0<=k & k < _i & 
	                 h[_a, idxInt(k)] = h[_this, F_SumMax_max])) &
	     h[_this, F_SumMax_sum] <= _i * h[_this, F_SumMax_max] &
	     h[_this, F_SumMax_sum] = (\sum k; 0; _i; h[_a, idxInt(k)])) & modHeap(h, ho, {h:=ho}(singleton(_this)))), arrlen(_a) - _i
   sourceline 39
    (* ILOAD *)
    st_0_int := _i
    (* ALOAD *)
    st_1_ref := _a
    (* ARRAYLENGTH *)
    assert !st_1_ref = null ; "non-null array reference"
    st_1_int := arrlen(st_1_ref)
    (* IF_ICMPGE *)
    branchCond := st_0_int >= st_1_int
    goto Label6, Label7
  Label6:
    assume branchCond
    goto Label8
  Label7:
    assume !branchCond
  Label9:
   sourceline 40
    (* ALOAD *)
    st_0_ref := _this
    (* GETFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    st_0_int := h[st_0_ref, F_SumMax_max]
    (* ALOAD *)
    st_1_ref := _a
    (* ILOAD *)
    st_2_int := _i
    (* IALOAD *)
    assert !st_1_ref = null ; "non-null array access"
    assert st_2_int >= 0 & st_2_int < arrlen(st_1_ref) ; "array index in bounds"
    st_1_int := h[st_1_ref, idxInt(st_2_int)] ; "read value from array"
    (* IF_ICMPGE *)
    branchCond := st_0_int >= st_1_int
    goto Label10, Label11
  Label10:
    assume branchCond
    goto Label12
  Label11:
    assume !branchCond
  Label13:
   sourceline 41
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _a
    (* ILOAD *)
    st_2_int := _i
    (* IALOAD *)
    assert !st_1_ref = null ; "non-null array access"
    assert st_2_int >= 0 & st_2_int < arrlen(st_1_ref) ; "array index in bounds"
    st_1_int := h[st_1_ref, idxInt(st_2_int)] ; "read value from array"
    (* PUTFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    assert st_0_ref :: modSet ; "object is assignable"
    h := h[st_0_ref, F_SumMax_max:= st_1_int]
  Label12:
   sourceline 43
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _this
    (* GETFIELD *)
    assert !st_1_ref = null ; "non-null field access receiver"
    st_1_int := h[st_1_ref, F_SumMax_sum]
    (* ALOAD *)
    st_2_ref := _a
    (* ILOAD *)
    st_3_int := _i
    (* IALOAD *)
    assert !st_2_ref = null ; "non-null array access"
    assert st_3_int >= 0 & st_3_int < arrlen(st_2_ref) ; "array index in bounds"
    st_2_int := h[st_2_ref, idxInt(st_3_int)] ; "read value from array"
    (* IADD *)
    st_1_int := st_1_int + st_2_int
    (* PUTFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    assert st_0_ref :: modSet ; "object is assignable"
    h := h[st_0_ref, F_SumMax_sum:= st_1_int]
  Label14:
   sourceline 44
    (* IINC *)
    _i := _i + 1
    (* GOTO *)
    goto Label5
  Label8:
   sourceline 46
    (* RETURN *)
    exc := null
    goto LabelEnd
  Label15:
  LabelEnd:
    assert exc = null ; "No exception has been thrown"
   sourceline 11
    assert (\forall k; 0<=k & k<N ->
                        h[_a, idxInt(k)] <= h[_this, F_SumMax_max]) ; "Assert postcondition"
   sourceline 13
    assert N > 0 -> 
                   (\exists k; 0<=k & k < N & 
                        h[_a, idxInt(k)] = h[_this, F_SumMax_max]) ; "Assert postcondition"
   sourceline 16
    assert h[_this, F_SumMax_sum] <= N * h[_this, F_SumMax_max] ; "Assert postcondition"
   sourceline 17
    assert h[_this, F_SumMax_sum] = (\sum k; 0; N; h[_a, idxInt(k)]) ; "Assert postcondition"
(* end of program *)
