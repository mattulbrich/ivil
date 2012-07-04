(*
 * Ad hoc definitions for the refinement proof
 * of Sum to sum
 *)

include "$base.p"
include "java-out/jbc_preamble.p"

plugin
  prettyPrinter: "de.uka.iti.pseudo.prettyprint.plugin.JavaHeapPrettyPrinter"

function seq(int) arrayAsIntSeq(heap, ref)

rule inArrayAsIntSeq
  find arrayAsIntSeq(%h, %r)
  where freshVar %x, %h, %o
  replace (\seqDef %x; 0; %h[%r, $array_length]; 
      %h[%r, $array_index(%x)])

rule getOfInArrayAsIntSeq
  find seqGet(arrayAsIntSeq(%h, %r), %i)
  replace cond(0<=%i & %i < %h[%r,$array_length],
               %h[%r, $array_index(%i)], seqError)
  tags 
    derived
    rewrite "fol simp"
    asAxiom

rule lenOfInArrayAsIntSeq
  find seqLen(arrayAsIntSeq(%h, %a))
  replace %h[%a, $array_length]
  tags
    derived
    rewrite "fol simp"
    asAxiom

rule OOPS
  closegoal