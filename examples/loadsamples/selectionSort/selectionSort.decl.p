# needed by selectionSort.p

include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"
  "$seq.p"
#  "$bytecode.p"

plugin 
  prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.SeqPrettyPrinter"

properties 
  # SMTStrategy.closingRule "patient_smt"
  SimplificationStrategy.splitMode "DONT_SPLIT"

function
  seq(int) seqId(int)
  seq('a) seqSwap(seq('a), int, int)
  seq(int) seqInv(seq(int))

  bool seqDom(seq('a), int)

  bool isPerm(seq('a), seq('a))
  bool isPermN(seq(int))
  bool isSorted(seq(int))
  int count(seq('a), 'a)

(*
 * definitions ... rules are axioms
 *)

rule seqSwapDef
  find seqSwap(%a, %i, %j)
  where freshVar %t, %a, %i, %j
  replace (\seqDef %t; 0; seqLen(%a);
     cond(%t = %i, seqGet(%a, %j),
       cond(%t = %j, seqGet(%a, %i), seqGet(%a, %t))))

rule seqIdDef
  find seqId(%n)
  where freshVar %t, %n
  replace (\seqDef %t; 0; %n; %t)

rule inDom_def
  find seqDom(%a, %i)
  replace 0 <= %i & %i < seqLen(%a)
  tags asAxiom

rule isPermN_def
  find isPermN(%p)
  replace (\forall i; seqDom(%p, i) -> 
      (\exists j; seqDom(%p, j) & seqGet(%p, j) = i))

rule isPerm_def
  find isPerm(%a, %b)
  replace seqLen(%a) = seqLen(%b) 
    & (\exists p; isPermN(p) & seqLen(p) = seqLen(%a)
       & (\forall i; seqDom(p, i) ->
            seqGet(%a, i) = seqGet(%b, seqGet(p, i))))

rule seqInvDef
  find seqInv(%s)
  where freshVar %i, %s
  where freshVar %j, %s
  replace (\seqDef %i; 0; seqLen(%s);
            (\some %j; seqDom(%s, %j) & seqGet(%s,%j) = %i))

(*
 * derived rules
 *)

rule lenSeqId
  find seqLen(seqId(%n))
  replace cond(%n >= 0, %n, 0)
  tags 
    asAxiom
    derived
    rewrite "fol simp"

#  tags asAxiom

rule getOfId
  find seqGet(seqId(%n), %i)
  replace cond(0 <= %i & %i < %n, %i, seqError)
  tags 
    asAxiom
    derived

axiom isSorted_def
 (\forall a; isSorted(a) =
   (\forall i; seqDom(a, i) -> 
     (\forall j; seqDom(a, j) & i < j -> seqGet(a, i) <= seqGet(a, j))))

rule isSorted_def
  find isSorted(%a)
  where freshVar %i, %a
  where freshVar %j, %a
  replace (\forall %i; seqDom(%a, %i) -> 
     (\forall %j; seqDom(%a, %j) & %i < %j -> seqGet(%a, %i) <= seqGet(%a, %j)))
#  tags asAxiom

(*
 * derived simplification rules
 *)

rule getOfSwap
  find seqGet(seqSwap(%a, %i, %j), %k)
  replace cond(0 <= %k & %k < seqLen(%a),
           cond(%i = %k, seqGet(%a, %j), 
            cond(%j = %k, seqGet(%a, %i), seqGet(%a, %k))), seqError)
  tags 
    derived
    rewrite "fol simp"
    asAxiom

rule lenOfSwap
  find seqLen(seqSwap(%a, %i, %j))
  replace seqLen(%a)
  tags 
    derived
    rewrite "fol simp"
    asAxiom

rule inDomSwap
  find seqDom(seqSwap(%a, %i, %j), %k)
  replace seqDom(%a, %k)
  tags derived
       rewrite "fol simp"

rule inDomId
  find seqDom(seqId(%n), %i)
  replace 0 <= %i & %i < %n
  tags derived
       rewrite "fol simp"

rule isPermN_read_preserves_inDom
  assume isPermN(%x) |-
  find |- seqDom(%x, seqGet(%x,%i))
  replace seqDom(%x, %i)
  tags
    derived

rule isPermN_swap
  find |- isPermN(seqSwap(%a, %i, %j))
  replace isPermN(%a) & seqDom(%a, %i) & seqDom(%a, %j)
  tags derived

rule isPermN_id
  find isPermN(seqId(%n))
  replace true
  tags derived
       rewrite "concrete"

rule isPerm_trans
  assume isPerm(%a,%b) |-
  find isPerm(%b,%c) |-
  add isPerm(%a,%c) |-
  tags 
    derived
    asAxiom

rule isPerm_symm
  find isPerm(%b,%a)
  replace isPerm(%a,%b)
  tags asAxiom derived


rule isPerm_refl
  assume %a = %b |-
  find isPerm(%a, %b)
  replace true
  tags derived
       rewrite "fol simp"  

rule isPerm_refl2
  find isPerm(%a, %a)
  replace true
  tags derived 
       rewrite "fol simp"

rule isPerm_swap
  find |- isPerm(seqSwap(%a, %i, %j), %b)
  replace isPerm(%a, %b) & seqDom(%a, %i) & seqDom(%a, %j)
  tags derived
       rewrite "fol simp"

rule isPerm_swap2
  find |- isPerm(%b, seqSwap(%a, %i, %j))
  replace isPerm(%a, %b) & seqDom(%a, %i) & seqDom(%a, %j)
  tags derived
       rewrite "fol simp"

rule permInjective
  assume isPermN(%p) |-
  assume seqDom(%p, %i) |-
  assume seqDom(%p, %j) |-
  find seqGet(%p, %i) = seqGet(%p, %j)
  replace %i = %j
  tags derived

rule invPermIsId
  assume isPermN(%p) |-
  assume seqDom(%p, %i) |-
  find seqGet(seqInv(%p), seqGet(%p, %i))
  replace %i
  tags derived
