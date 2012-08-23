(*
 * Ad hoc definitions for the refinement proof
 * of Sum to sum
 *)

include "$base.p"
include "$seq.p"
include "$heap.p"
include "$refinement.p"
include "$symbex.p"
include "$decproc.p"
include "$set.p"
include "$seq.p"

# plugin
#   prettyPrinter: "de.uka.iti.pseudo.prettyprint.plugin.JavaHeapPrettyPrinter"

function seq(int) arrayAsIntSeq(heap, ref)

rule inArrayAsIntSeq
  find arrayAsIntSeq(%h, %r)
  where freshVar %x as int, %h, %o
  replace (\seqDef %x; 0; arrlen(%r); 
      %h[%r, idxInt(%x)])

rule getOfInArrayAsIntSeq
  find seqGet(arrayAsIntSeq(%h, %r), %i)
  replace cond(0<=%i & %i < arrlen(%r),
               %h[%r, idxInt(%i)], seqError)
  tags 
    derived
    rewrite "fol simp"
    asAxiom

rule lenOfInArrayAsIntSeq
  find seqLen(arrayAsIntSeq(%h, %a))
  replace arrlen(%a)
  tags
    derived
    rewrite "fol simp"
    asAxiom

rule OOPS
  closegoal