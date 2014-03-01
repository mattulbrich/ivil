(* created : Thu Mar 14 21:32:54 CET 2013 - Alpha-3 *)
#
# This file was generated by PSEUDO
# which is copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#

include "$bytecode.p"
include "$intRange.p"
include "declarations.p"

(* program variables *)
function
   int _src assignable (* local variable '1' *)
   int pre_src assignable (* method parameter, prestate *)
   int _dest assignable (* method parameter / ... *)
   int pre_dest assignable (* method parameter, prestate *)
   ref st_0_ref assignable (* stack variable *)
   int st_0_int assignable (* stack variable *)
   ref _V assignable (* local variable '3' *)
   ref _C assignable (* local variable '4' *)
   ref _N assignable (* local variable '5' *)
   int st_1_int assignable (* stack variable *)
   bool st_2_bool assignable (* stack variable *)
   int st_2_int assignable (* stack variable *)
   bool st_0_bool assignable (* stack variable *)
   int _d assignable (* local variable '6' *)
   heap ho1 assignable (* Heap snapshot for loop invariant *)
   ref st_1_ref assignable (* stack variable *)
   bool branchCond assignable (* branching condition *)
   int _v assignable (* local variable '7' *)
   heap ho2 assignable (* Heap snapshot for loop invariant *)
   int _w assignable (* local variable '8' *)
   heap ho3 assignable (* Heap snapshot for loop invariant *)
   ref st_2_ref assignable (* stack variable *)

(* the translated Java program *)
program Java source "./BFS.jspec"
   sourceline 92
    assume !h[_this, F_BFS_adjacency] = null ; "Assume precondition"
   sourceline 93
    assume h[_this, F_BFS_size] > 0 ; "Assume precondition"
   sourceline 94
    assume arrlen(h[_this, F_BFS_adjacency]) = h[_this, F_BFS_size] ; "Assume precondition"
   sourceline 95
    assume (\forall i; 0<=i & i < h[_this, F_BFS_size] ->
                    !h[h[_this, F_BFS_adjacency], idxRef(i)] = null
                  &   arrlen(h[h[_this, F_BFS_adjacency], idxRef(i)]) 
                    = h[_this, F_BFS_size]) ; "Assume precondition"
   sourceline 99
    assume 0 <= _src & _src < h[_this, F_BFS_size] ; "Assume precondition"
   sourceline 100
    assume 0 <= _dest & _dest < h[_this, F_BFS_size] ; "Assume precondition"
    assume wellformed(h) ; "assumption guaranteed by Java"
    assume !_this = null ; "assumption guaranteed by Java"
    assume h[_this, created] ; "assumption guaranteed by Java"
    assume typeof(_this) = C_BFS ; "assumption of the runtime type"
    pre_h := h || pre_src := _src || pre_dest := _dest ; "Recording the prestate"
    decrBase := 1 ; "Save the decrease (variant) base"
  Label1:
   sourceline 107
    (* ALOAD *)
    st_0_ref := _this
    (* GETFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    st_0_int := h[st_0_ref, F_BFS_size]
    (* NEWARRAY *)
    assert st_0_int >= 0 ; "non-negative array length"
    havoc new
    assume !h[new, created]
    assume arrlen(new) = st_0_int
    assume typeof(new) = array_boolean
    h := create(h, new)
    h := h[new, created := true]
    st_0_ref := new
    (* ASTORE *)
    _V := st_0_ref
  Label2:
   sourceline 108
    (* ALOAD *)
    st_0_ref := _this
    (* GETFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    st_0_int := h[st_0_ref, F_BFS_size]
    (* NEWARRAY *)
    assert st_0_int >= 0 ; "non-negative array length"
    havoc new
    assume !h[new, created]
    assume arrlen(new) = st_0_int
    assume typeof(new) = array_boolean
    h := create(h, new)
    h := h[new, created := true]
    st_0_ref := new
    (* ASTORE *)
    _C := st_0_ref
  Label3:
   sourceline 109
    (* ALOAD *)
    st_0_ref := _this
    (* GETFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    st_0_int := h[st_0_ref, F_BFS_size]
    (* NEWARRAY *)
    assert st_0_int >= 0 ; "non-negative array length"
    havoc new
    assume !h[new, created]
    assume arrlen(new) = st_0_int
    assume typeof(new) = array_boolean
    h := create(h, new)
    h := h[new, created := true]
    st_0_ref := new
    (* ASTORE *)
    _N := st_0_ref
  Label4:
   sourceline 111
    (* LDC + INVOKESTATIC - Specification injection *)
    assert !_V = _C
assume !_V = _C
  Label5:
   sourceline 112
    (* LDC + INVOKESTATIC - Specification injection *)
    assert !_V = _N
assume !_V = _N
  Label6:
   sourceline 113
    (* LDC + INVOKESTATIC - Specification injection *)
    assert !_N = _C
assume !_N = _C
  Label7:
   sourceline 115
    (* ALOAD *)
    st_0_ref := _V
    (* ILOAD *)
    st_1_int := _src
    (* ICONST_1 *)
    st_2_bool := true
    st_2_int := 1
    (* BASTORE *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    h := h[st_0_ref, idxBool(st_1_int) := st_2_bool]
  Label8:
   sourceline 116
    (* ALOAD *)
    st_0_ref := _C
    (* ILOAD *)
    st_1_int := _src
    (* ICONST_1 *)
    st_2_bool := true
    st_2_int := 1
    (* BASTORE *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    h := h[st_0_ref, idxBool(st_1_int) := st_2_bool]
  Label9:
   sourceline 117
    (* ICONST_0 *)
    st_0_bool := false
    st_0_int := 0
    (* ISTORE *)
    _d := st_0_int
  Label10:
   sourceline 119
    (* LDC + INVOKESTATIC - Specification injection *)
    ho1 := h
    (* spec statement deferred till after looping point *)
  Label11:
    (* deferred specification statement *)
    skip LOOPINV, {ho := ho1}((_d >= 0) & modHeap(h, ho, {h:=ho}(singleton(_V) \/ singleton(_C) \/ singleton(_N)))), 2
   sourceline 123
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _C
    (* INVOKEVIRTUAL *)
    assert !st_1_ref = null ; "show precondition for BFS.isEmpty"
    assert $prec(0, decrBase) ; "variant decreased for BFS.isEmpty"
    havoc resBool
    st_0_bool := resBool
    ho := h
    havoc h
    assume wellformed(h)
    skip (* unsupported emptyset *)
    assume modHeap(h, ho, {h:=ho}(emptyset))
    assume resBool = !(\exists i; 0 <= i & i < arrlen(st_1_ref) & 
                                  h[st_1_ref, idxBool(i)]) ; "postcondition for BFS.isEmpty"
    (* IFNE *)
    branchCond := st_0_bool
    goto Label12, Label13
  Label12:
    assume branchCond
    goto Label14
  Label13:
    assume !branchCond
  Label15:
   sourceline 124
    (* LDC + INVOKESTATIC - Specification injection *)
  Label16:
   sourceline 125
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _C
    (* ICONST_0 *)
    st_2_bool := false
    st_2_int := 0
    (* INVOKEVIRTUAL *)
    assert !st_1_ref = null ; "show precondition for BFS.first"
    assert $prec(0, decrBase) ; "variant decreased for BFS.first"
    havoc resInt
    st_0_int := resInt
    ho := h
    havoc h
    assume wellformed(h)
    skip (* unsupported emptyset *)
    assume modHeap(h, ho, {h:=ho}(emptyset))
    assume -1 = resInt | st_2_int <= resInt & resInt < arrlen(st_1_ref) ; "postcondition for BFS.first"
    assume resInt >= st_2_int -> h[st_1_ref, idxBool(resInt)] &
                     (\forall i; st_2_int<=i & i<resInt -> !h[st_1_ref, idxBool(i)]) ; "postcondition for BFS.first"
    assume resInt = -1 -> (\forall i; 0<=i & i<arrlen(st_1_ref) -> 
                                  !h[st_1_ref,idxBool(i)]) ; "postcondition for BFS.first"
    (* ISTORE *)
    _v := st_0_int
  Label17:
   sourceline 128
    (* LDC + INVOKESTATIC - Specification injection *)
    assert 0 <= _v & _v < h[_this, F_BFS_size]
assume 0 <= _v & _v < h[_this, F_BFS_size]
  Label18:
   sourceline 129
    (* ALOAD *)
    st_0_ref := _C
    (* ILOAD *)
    st_1_int := _v
    (* ICONST_0 *)
    st_2_bool := false
    st_2_int := 0
    (* BASTORE *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    h := h[st_0_ref, idxBool(st_1_int) := st_2_bool]
  Label19:
   sourceline 130
    (* ILOAD *)
    st_0_int := _v
    (* ILOAD *)
    st_1_int := _dest
    (* IF_ICMPNE *)
    branchCond := !st_0_int = st_1_int
    goto Label20, Label21
  Label20:
    assume branchCond
    goto Label22
  Label21:
    assume !branchCond
  Label23:
   sourceline 131
    (* ILOAD *)
    st_0_int := _d
    (* IRETURN *)
    exc := null
    resInt := st_0_int
    goto LabelEnd
  Label22:
   sourceline 134
    (* LDC + INVOKESTATIC - Specification injection *)
    ho2 := h
    (* spec statement deferred till after looping point *)
  Label24:
   sourceline 138
    (* ICONST_0 *)
    st_0_bool := false
    st_0_int := 0
    (* ISTORE *)
    _w := st_0_int
  Label25:
    (* deferred specification statement *)
    skip LOOPINV, {ho := ho2}((0 <= _w (* & _w <= h[_this, F_BFS_size] *)) & modHeap(h, ho, {h:=ho}(singleton(_V) \/ singleton(_N)))), 2
    (* ILOAD *)
    st_0_int := _w
    (* ALOAD *)
    st_1_ref := _this
    (* GETFIELD *)
    assert !st_1_ref = null ; "non-null field access receiver"
    st_1_int := h[st_1_ref, F_BFS_size]
    (* IF_ICMPGE *)
    branchCond := st_0_int >= st_1_int
    goto Label26, Label27
  Label26:
    assume branchCond
    goto Label28
  Label27:
    assume !branchCond
  Label29:
   sourceline 139
    (* LDC + INVOKESTATIC - Specification injection *)
  Label30:
   sourceline 142
    (* LDC + INVOKESTATIC - Specification injection *)
    ho3 := h
    (* spec statement deferred till after looping point *)
  Label31:
    (* deferred specification statement *)
    skip LOOPINV, {ho := ho3}((0 <= _w & _w <= h[_this, F_BFS_size]) & modHeap(h, ho, {h:=ho}(singleton(_V) \/ singleton(_N)))), 2
   sourceline 146
    (* ILOAD *)
    st_0_int := _w
    (* ALOAD *)
    st_1_ref := _this
    (* GETFIELD *)
    assert !st_1_ref = null ; "non-null field access receiver"
    st_1_int := h[st_1_ref, F_BFS_size]
    (* IF_ICMPGE *)
    branchCond := st_0_int >= st_1_int
    goto Label32, Label33
  Label32:
    assume branchCond
    goto Label34
  Label33:
    assume !branchCond
    (* ALOAD *)
    st_0_ref := _this
    (* GETFIELD *)
    assert !st_0_ref = null ; "non-null field access receiver"
    st_0_ref := h[st_0_ref, F_BFS_adjacency]
    (* ILOAD *)
    st_1_int := _v
    (* AALOAD *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    st_0_ref := h[st_0_ref, idxRef(st_1_int)] ; "read value from array"
    (* ILOAD *)
    st_1_int := _w
    (* BALOAD *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    st_0_bool := h[st_0_ref, idxBool(st_1_int)] ; "read value from array"
    (* IFNE *)
    branchCond := st_0_bool
    goto Label35, Label36
  Label35:
    assume branchCond
    goto Label34
  Label36:
    assume !branchCond
  Label37:
   sourceline 147
    (* IINC *)
    _w := _w + 1
  Label38:
   sourceline 149
    (* LDC + INVOKESTATIC - Specification injection *)
    skip
    (* GOTO *)
    goto Label31
  Label34:
   sourceline 152
    (* LDC + INVOKESTATIC - Specification injection *)
  Label39:
   sourceline 154
    (* ILOAD *)
    st_0_int := _w
    (* ALOAD *)
    st_1_ref := _this
    (* GETFIELD *)
    assert !st_1_ref = null ; "non-null field access receiver"
    st_1_int := h[st_1_ref, F_BFS_size]
    (* IF_ICMPGE *)
    branchCond := st_0_int >= st_1_int
    goto Label40, Label41
  Label40:
    assume branchCond
    goto Label42
  Label41:
    assume !branchCond
    (* ALOAD *)
    st_0_ref := _V
    (* ILOAD *)
    st_1_int := _w
    (* BALOAD *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    st_0_bool := h[st_0_ref, idxBool(st_1_int)] ; "read value from array"
    (* IFNE *)
    branchCond := st_0_bool
    goto Label43, Label44
  Label43:
    assume branchCond
    goto Label42
  Label44:
    assume !branchCond
  Label45:
   sourceline 155
    (* ALOAD *)
    st_0_ref := _V
    (* ILOAD *)
    st_1_int := _w
    (* ICONST_1 *)
    st_2_bool := true
    st_2_int := 1
    (* BASTORE *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    h := h[st_0_ref, idxBool(st_1_int) := st_2_bool]
  Label46:
   sourceline 156
    (* ALOAD *)
    st_0_ref := _N
    (* ILOAD *)
    st_1_int := _w
    (* ICONST_1 *)
    st_2_bool := true
    st_2_int := 1
    (* BASTORE *)
    assert !st_0_ref = null ; "non-null array access"
    assert st_1_int >= 0 & st_1_int < arrlen(st_0_ref) ; "array index in bounds"
    h := h[st_0_ref, idxBool(st_1_int) := st_2_bool]
  Label42:
   sourceline 159
    (* LDC + INVOKESTATIC - Specification injection *)
  Label47:
   sourceline 138
    (* IINC *)
    _w := _w + 1
    (* GOTO *)
    goto Label25
  Label28:
   sourceline 161
    (* LDC + INVOKESTATIC - Specification injection *)
  Label48:
   sourceline 162
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _C
    (* INVOKEVIRTUAL *)
    assert !st_1_ref = null ; "show precondition for BFS.isEmpty"
    assert $prec(0, decrBase) ; "variant decreased for BFS.isEmpty"
    havoc resBool
    st_0_bool := resBool
    ho := h
    havoc h
    assume wellformed(h)
    skip (* unsupported emptyset *)
    assume modHeap(h, ho, {h:=ho}(emptyset))
    assume resBool = !(\exists i; 0 <= i & i < arrlen(st_1_ref) & 
                                  h[st_1_ref, idxBool(i)]) ; "postcondition for BFS.isEmpty"
    (* IFEQ *)
    branchCond := !st_0_bool
    goto Label49, Label50
  Label49:
    assume branchCond
    goto Label51
  Label50:
    assume !branchCond
  Label52:
   sourceline 163
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _C
    (* ALOAD *)
    st_2_ref := _N
    (* INVOKEVIRTUAL *)
    assert !st_1_ref = null ; "show precondition for BFS.copy"
    assert !st_2_ref = null ; "show precondition for BFS.copy"
    assert arrlen(st_2_ref) = arrlen(st_1_ref) ; "show precondition for BFS.copy"
    assert $prec(0, decrBase) ; "variant decreased for BFS.copy"
    ho := h
    havoc h
    assume wellformed(h)
    skip (* unsupported singleton(target) *)
    assume modHeap(h, ho, {h:=ho}(singleton(st_1_ref)))
    assume (\forall i; 0 <= i & i < arrlen(st_1_ref) -> 
           h[st_1_ref, idxBool(i)] = h[st_2_ref, idxBool(i)]) ; "postcondition for BFS.copy"
  Label53:
   sourceline 164
    (* ALOAD *)
    st_0_ref := _this
    (* ALOAD *)
    st_1_ref := _N
    (* INVOKEVIRTUAL *)
    assert !st_1_ref = null ; "show precondition for BFS.clear"
    assert $prec(0, decrBase) ; "variant decreased for BFS.clear"
    ho := h
    havoc h
    assume wellformed(h)
    skip (* unsupported singleton(array) *)
    assume modHeap(h, ho, {h:=ho}(singleton(st_1_ref)))
    assume (\forall i; 0 <= i & i < arrlen(st_1_ref) -> 
                                  !h[st_1_ref, idxBool(i)]) ; "postcondition for BFS.clear"
  Label54:
   sourceline 165
    (* IINC *)
    _d := _d + 1
  Label51:
   sourceline 168
    (* LDC + INVOKESTATIC - Specification injection *)
  Label55:
   sourceline 169
    (* GOTO *)
    goto Label11
  Label14:
   sourceline 171
    (* ICONST_M1 *)
    st_0_int := -1
    (* IRETURN *)
    exc := null
    resInt := st_0_int
    goto LabelEnd
  Label56:
  LabelEnd:
    assert exc = null ; "No exception has been thrown"
   sourceline 101
    assert -1 <= resInt ; "Assert postcondition"
   sourceline 102
    assert modHeap(h, pre_h, {h:=pre_h}(freshObjects(h)))
(* end of program *)
