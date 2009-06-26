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

program P
  source "PRE: \forall Node n; n.size >= 0"
    assume (\forall k; R(H,k,field_size) >= 0)
  source "PRE: head != null"
    assume !head = null
  source ""
  source "int sumList(Node head) {"
  source "   int sum = 0;"
    sum := 0
  source "   Node n = head;"
    n := head

  source "   while(n != null) {"
   loop:
    skip
    goto body, after
   body:
    assume !n = null
  source "      sum += n.size;"
    assert !n = null
    sum := sum + R(H, n, field_size)
  source "      n = n.next;"
    assert !n = null
    n := R(H, n, field_next)
  source "   }"
    goto loop

   after:
  source "   heap.done = true;"
    assert ! head = null
    H := W(H, head, field_done, true)
  source "}"
  source ""
  source "POST: sum > 0 && head.length == \old(head.length)"
    assert sum >= 0 & length(H, head) = length(heapBegin, head)

problem
 (\forall k as ref; 
   (\forall h1 as heap;
     (\forall h2 as heap;
       R(h1,k,field_next) = R(h2,k,field_next) -> 
       length(h1, k) = length(h2, k))))
  & heapBegin = H -> [0; P]

