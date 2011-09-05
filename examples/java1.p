include "$int.p" 
  "$fol.p"
  "$symbex.p"
  "$heap.p"
  "$decproc.p"

function 
  ref head assignable
  ref this
  ref n assignable
  int sum assignable
  int result assignable
  heap h assignable

  int len(heap, ref)
  heap heapBegin

  field(int) field_size
  field(ref) field_next
  field(bool) field_done

rule positive_sum
  assume %a >= 0 |-
  assume %b >= 0 |-
  find %a + %b >= 0
  replace true

program P source "./java1.java"

    sourceline 11
    assume !this = null
    assume (\forall t; sel(h,t,field_size) >= 0)

    sourceline 16
    sum := 0

    sourceline 17
    n := this

    sourceline 18
   loop:
    skip
    goto body, after
   body:
    assume !n = null

    sourceline 19
    assert !n = null
    sum := sum + sel(h, n, field_size)

    sourceline 20
    assert !n = null
    n := sel(h, n, field_next)

    sourceline 21
    goto loop

   after:
    sourceline 22
    result := sum

    sourceline 12
    assert result >= 0 

    sourceline 13
    assert (\forall o; (\forall f; sel(h, o, f) = sel(heapBegin, o, f)))

problem
  heapBegin = h -> [0; P]true

