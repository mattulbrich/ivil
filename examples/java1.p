include "$int.p" 
  "$fol.p"
  "$symbex.p"
  "$heap.p"
  "$decproc.p"

function 
  ref head assignable
  ref n assignable
  int sum assignable

  int length(heap, ref)
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
    assume (\forall k; R(H,k,field_size) >= 0)
    assume !head = null
    sum := 0
    n := head

   loop:
    skip
    goto body, after
   body:
    assume !n = null
    assert !n = null
    sum := sum + R(H, n, field_size)
    assert !n = null
    n := R(H, n, field_next)
    goto loop

   after:
    assert ! head = null
    H := W(H, head, field_done, true)
    assert sum >= 0 & length(H, head) = length(heapBegin, head)

problem
 (\forall k as ref; 
   (\forall h1 as heap;
     (\forall h2 as heap;
       R(h1,k,field_next) = R(h2,k,field_next) -> 
       length(h1, k) = length(h2, k))))
  & heapBegin = H -> [0; P]

