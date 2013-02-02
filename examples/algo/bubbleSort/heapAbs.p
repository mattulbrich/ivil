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
include "selectionSort.decl.p"

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

rule lemma_for_selectionSort
  find |- seqSwap(%b, %i, %t) = arrayAsIntSeq((%h as heap)[%a, idxInt(%_i) := %h[%a, idxInt(%_t)]]
                                                          [%a, idxInt(%_t) := %h[%a, idxInt(%_i)]], %a)
  replace %b = arrayAsIntSeq(%h, %a) & %i = %_i & %t = %_t & seqDom(%b, %i) & seqDom(%b, %t)
  tags 
    derived
    rewrite "fol simp"

rule OOPS
  closegoal