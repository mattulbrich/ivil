
include "$int.p"
        "$set.p"
        "$seqdefs.p"
        
(*
 * Lemmata and general rules
 *)

rule seqSubSelf
  find seqSub(%a, 0, seqLen(%a))
  replace %a
  tags
    derived
    rewrite "concrete"

rule seqOutside1
  assume %i < 0 |-
  find seqGet(%a, %i)
  replace seqError
  tags
    derived

rule seqOutside2
  assume %i >= seqLen(%a) |-
  find seqGet(%a, %i)
  replace seqError
  tags
    derived

rule lenOfSeqEmpty
  find seqLen(seqEmpty)
  replace 0        
  tags
    derived
    rewrite "concrete"

rule getOfSeqEmpty
  find seqGet(seqEmpty, %i)
  replace seqError
  tags
    derived
    rewrite "concrete"

rule lenOfSeqSingleton
  find seqLen(seqSingleton(%x))
  replace 1
  tags
    derived
    rewrite "fol simp"

rule getOfSeqSingleton
  find seqGet(seqSingleton(%x), %i)
  replace cond(%i = 0, %x, seqError)
  tags
    derived
    rewrite "fol simp"

rule lenOfSeqConcat
  find seqLen(seqConcat(%seq, %seq2))
  replace seqLen(%seq) + seqLen(%seq2)
  tags
    derived
    rewrite "fol simp"

rule seqLenOfSub
  find seqLen(seqSub(%a, %from, %to))
  replace cond(%from <= %to, %to-%from, 0)
  tags 
    derived
    rewrite "fol simp"

rule seqGetOfSub
  find seqGet(seqSub(%a, %from, %to), %i)
  replace cond(0 <= %i & %i < %to-%from, 
            seqGet(%a, %i+%from), seqError)
  tags 
    derived 
    rewrite "fol simp"

rule lenOfSeqReverse
  find seqLen(seqReverse(%seq))
  replace seqLen(%seq)
  tags 
    derived
    rewrite "fol simp"

rule getOfSeqReverse
  find seqGet(seqReverse(%seq), %i)
  replace seqGet(%seq, seqLen(%seq) - 1 - %i)
  tags
    derived
    rewrite "fol simp"

(*
 * lemmata for seqEmpty
 *)
    
rule seqConcatWithSeqEmpty1
  find seqConcat(%seq, seqEmpty)
  replace (%seq)
  tags
    rewrite "concrete"
    derived
    
rule seqConcatWithSeqEmpty2
  find seqConcat(seqEmpty, %seq)
  replace (%seq)
  tags
    rewrite "concrete"
    derived

rule seqReverseOfSeqEmpty
  find seqReverse(seqEmpty)
  replace seqEmpty
  tags
    rewrite "concrete"
    derived

rule seqReverseOfReverse
  find seqReverse(seqReverse(%s))
  replace %s
  tags
    rewrite "concrete"
    derived

rule seqSubEmpty
  find seqSub(%s, %a, %a)
  replace seqEmpty
  tags
    rewrite "concrete"
    derived

(*
 * other lemmata
 *)
 
