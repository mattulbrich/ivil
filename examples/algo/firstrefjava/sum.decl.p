(*
 * Ad hoc definitions for the refinement proof
 * of Sum to sum
 *)

include "java-out/jbc_preamble.p"

function seq(int) arrayAsIntSeq(heap, ref)

rule inArrayAsIntSet
  find arrayAsIntSeq(%h, %r)
  where freshVar %x, %h, %o
  replace (\seqDef %x; 0; %h[%r, $array_length]; 
      %h[%r, $array_index(%x)])