
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

problem
  R(W(H, n, field_done, true), head, field_next) = R(H, head, field_next)
