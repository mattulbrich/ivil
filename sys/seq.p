
include "$int.p"
        "$set.p"
        
sort
  seq('a)

function
  'a seqGet(seq('a), int)
  int seqLen(seq('a))
  int seqIndexOf(seq('a), 'a)
  'a seqError
  
binder
  (* primary constructor *)
  seq('a) (\seqDef int; int; int; 'a)

function
  (* secondary constructors *)
  seq('a) seqEmpty
  seq('a) seqSingleton('a)
  seq('a) seqConcat(seq('a), seq('a))
  seq('a) seqSub(seq('a), int, int)
  seq('a) seqReverse(seq('a))
  set('a) seqAsSet(seq('a))

(*
 * Axioms
 *)

rule seqGetDef
  find seqGet((\seqDef %i; %a; %b; %t), %j)
  replace cond(%a <= %j & %j < %b, 
           $$subst(%i, %j+%a, %t),
           seqError)

rule seqLenDef
  find seqLen((\seqDef %i; %a; %b; %t))
  replace cond(%a <= %b, %b-%a, 0)

axiom seqLenNonNeg
  (\T_all 'a; (\forall s as seq('a); seqLen(s) >= 0))

rule seqExtensionality
  find %s1 = %s2
  where freshVar %i, %s1, %s2
  replace seqLen(%s1) = seqLen(%s2) &
    (\forall %i; 0<=%i & %i < seqLen(%s1) 
        -> seqGet(%s1,%i) = seqGet(%s2,%i))

(*
 * derived constructors,
 * definitorial extensions
 *)
 
rule seqEmptyDef
  find seqEmpty as seq(%'a)
  replace (\seqDef x; 0; 0; seqError as %'a)

rule seqSingletonDef
  find seqSingleton(%val)
  where freshVar %i, %val
  replace (\seqDef %i; 0; 1; %val)

rule seqSubDef
  find seqSub(%a, %from, %to)
  where freshVar %x, %from, %to, %a
  replace (\seqDef %x; %from; %to; seqGet(%a, %x))

rule seqConcatDef
  find seqConcat(%a, %b)
  where freshVar %x, %a, %b
  replace (\seqDef %x; 0; seqLen(%a) + seqLen(%b); 
            cond(%x < seqLen(%a), seqGet(%a, %x), 
                                  seqGet(%b, %x-seqLen(%a))))

rule seqReverseDef
  find seqReverse(%a)
  where freshVar %x, %a
  replace (\seqDef %x; 0; seqLen(%a); seqGet(%a, seqLen(%a) - 1 - %x))

rule seqAsSetDef
  find seqAsSet(%s)
  where freshVar %x, %s
  where freshVar %i, %s
  replace (\set %x; (\exists %i; 0<=%i & %i<seqLen(%s) & 
                          seqGet(%s,%i)=%x))

(*
 * Lemmata and general rules
 *)

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
  replace cond(%from <= %i & %i < %to, 
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

rule inSeqAsSet
  find %x :: seqAsSet(%s)
  where freshVar %i, %s
  replace
    (\exists %i; 0<=%i & %i < seqLen(%s) & seqGet(%s, %i) = %x)
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

rule seqAsSetEmpty
  find seqAsSet(seqEmpty)
  replace emptyset
  tags 
    derived
    rewrite "concrete"

(*
 * other lemmata
 *)
 
rule subSeqComplete
  find seqSub(%seq, 0, seqLen(%seq))
  replace(%seq)
  tags
    derived
    rewrite "fol simp"

rule finiteSeqAsSet
  find finite(seqAsSet(%s))
  replace true
  tags
    derived
    rewrite "concrete"

rule cardSeqAsSet
  find card(seqAsSet(%s))
  replace seqLen(%s)
  tags
    derived
    rewrite "fol simp"