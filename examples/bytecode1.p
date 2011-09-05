(* created : Tue Oct 13 11:34:09 CEST 2009 *)
#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# which is copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#

include "$bytecode.p"

(* Database declarations *)
(* Program declarations *)
function
  (* local variables *)
  int _i assignable
  ref _a assignable
  field(ref) field_AList_elements
  reftype class_AList unique
  reftype class_java_lang_Object unique

(* Program *)
program P source "AList.java"
  stmt0: sourceline 29   (*    0: new[187](3) 6 *)
    havoc new
    assume !sel(h, new, created) & !new=null
    assume typeof(new) = class_AList
    h := newObject(h, new)
    h := stor(h, new, created, true)
    st := push(st, new)
  stmt3: sourceline 29   (*    3: dup[89](1) *)
    st := push(st, topRef(st))
  stmt4: sourceline 29   (*    4: invokespecial[183](3) 7 *)
    assert !topRef(st) = null
    assert true
    havoc anon
    h := disturb(h, anon, loc(topRef(st), field_AList_elements))
    assume sel(h,sel(h,topRef(st),field_AList_elements), length) = 0
    st := push(st, refRet)
  stmt7: sourceline 29   (*    7: astore_0[75](1) *)
    _a := topRef(st)
    st := pop(st)
  stmt8: sourceline 30   (*    8: aload_0[42](1) *)
    st := push(st, _a)
  stmt9: sourceline 30   (*    9: bipush[16](2) 100 *)
    st := push(st, 100)
  stmt11: sourceline 30   (*   11: invokevirtual[182](3) 8 *)
    assert !topRef(pop(st)) = null
    assert topInt(st) >= 0
    havoc anon
    h := disturb(h, anon, loc(topRef(st), field_AList_elements))
    assume sel(h, sel(h, topRef(pop(st)),field_AList_elements), length) = topInt(st) &
        (\forall n; 0<=n & n<topInt(st) ->
           sel(h, sel(h,topRef(pop(st)), field_AList_elements), refIdx(n)) = sel(h_pre, sel(h,topRef(pop(st)),field_AList_elements), refIdx(n)))
    st := pop2(st)
    st := push(st, refRet)
  stmt14: sourceline 31   (*   14: aload_0[42](1) *)
    st := push(st, _a)
  stmt15: sourceline 31   (*   15: iconst_0[3](1) *)
    st := push(st, 0)
  stmt16: sourceline 31   (*   16: aload_0[42](1) *)
    st := push(st, _a)
  stmt17: sourceline 31   (*   17: invokevirtual[182](3) 9 *)
    assert !topRef(pop2(st)) = null
    assert topInt(pop(st)) >= 0 & topInt(pop(st)) < sel(h,sel(h,topRef(pop2(st)),field_AList_elements), length)
    havoc anon
    h := disturb(h, anon, loc(sel(h, topRef(pop2(st)), field_AList_elements), refIdx(topInt(pop(st)))))
    assume sel(h, sel(h, topRef(pop2(st)), field_AList_elements), refIdx(topInt(pop(st)))) = topRef(st)
    st := pop2(st)
    st := push(st, refRet)
  stmt20: sourceline 32   (*   20: iconst_1[4](1) *)
    st := push(st, 1)
  stmt21: sourceline 32   (*   21: istore_1[60](1) *)
    _i :=  topInt(st)
    st := pop(st)
  stmt22: sourceline 32   (*   22: iload_1[27](1) *)
    st := push(st, _i)
  stmt23: sourceline 32   (*   23: bipush[16](2) 100 *)
    st := push(st, 100)
  stmt25: sourceline 32   (*   25: if_icmpge[162](3) -> aload_0 *)
    goto branch0, branch1
    branch0: assume topInt(pop(st)) >= topInt(st)
    st := pop2(st)
    goto stmt46
    branch1: assume !topInt(pop(st)) >= topInt(st)
    st := pop2(st)
  stmt28: sourceline 33   (*   28: aload_0[42](1) *)
    st := push(st, _a)
  stmt29: sourceline 33   (*   29: iload_1[27](1) *)
    st := push(st, _i)
  stmt30: sourceline 33   (*   30: new[187](3) 2 *)
    st := push(st, null)
  stmt33: sourceline 33   (*   33: dup[89](1) *)
    st := push(st, topRef(st))
  stmt37: sourceline 33   (*   37: invokevirtual[182](3) 9 *)
    assert !topRef(pop2(st)) = null
    assert topInt2(st) >= 0 & topInt2(st) < sel(h, sel(h, topRef3(st), field_AList_elements), length)
    havoc anon
    h := disturb(h, anon, loc(sel(h, topRef3(st), field_AList_elements), refIdx(topInt2(st))))
    assume sel(h, sel(h, topRef3(st), field_AList_elements), refIdx(topInt2(st))) = topRef(st)
    st := pop2(st)
    st := push(st, refRet)
  stmt40: sourceline 32   (*   40: iinc[132](3) 1 1 *)
    _i := _i + 1
  stmt43: sourceline 32   (*   43: goto[167](3) -> iload_1 *)
    goto stmt22
  stmt46: sourceline 35   (*   46: aload_0[42](1) *)
    st := push(st, _a)
  stmt47: sourceline 35   (*   47: iconst_2[5](1) *)
    st := push(st, 2)
  stmt48: sourceline 35   (*   48: invokevirtual[182](3) 8 *)
    assert !topRef2(st) = null
    assert topInt(st) >= 0
    havoc anon
    h := disturb(h, anon, everything)
    assume sel(h, sel(h, topRef2(st), field_AList_elements), length) = topInt(st) &
        (\forall n; 0<=n & n<topInt(st) ->
           sel(h, sel(h, topRef2(st), field_AList_elements), refIdx(n)) = sel(h_pre, sel(h, topRef2(st), field_AList_elements), refIdx(n)))
    st := pop(st)
    st := push(st, refRet)
  stmt51: sourceline 36   (*   51: aload_0[42](1) *)
    st := push(st, _a)
  stmt52: sourceline 36   (*   52: iconst_0[3](1) *)
    st := push(st, 0)
  stmt53: sourceline 36   (*   53: invokevirtual[182](3) 10 *)
    assert !topRef2(st) = null
    assert topInt(st) >= 0 & topInt(st) < sel(h,sel(h,topRef2(st),field_AList_elements), length)
    havoc anon
    h := disturb(h, anon, nothing)
    assume refRet = sel(h, sel(h,topRef2(st),field_AList_elements), refIdx(topInt(st)))
    st := pop(st)
    st := push(st, refRet)
  stmt56: sourceline 36   (*   56: aload_0[42](1) *)
    st := push(st, _a)
  stmt57: sourceline 36   (*   57: if_acmpne[166](3) -> iconst_0 *)
    goto branch2, branch3
    branch2: assume !topRef2(st) = topRef(st)
    st := pop2(st)
    goto stmt64
    branch3: assume ! !topRef2(st) = topRef(st)
    st := pop2(st)
  stmt60: sourceline 36   (*   60: iconst_1[4](1) *)
    st := push(st, 1)
  stmt61: sourceline 36   (*   61: goto[167](3) -> ireturn *)
    goto stmt65
  stmt64: sourceline 36   (*   64: iconst_0[3](1) *)
    st := push(st, 0)
  stmt65: sourceline 36   (*   65: ireturn[172](1) *)
    exc := null
    boolRet := topBool(st)
    st := pop(st)
    goto end_of_proc
    end_of_proc:
    assert boolRet = true
(* end of program *)

problem [0 ; P]true
