# needed by selectionSort.p

include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

sort 
  array('type)

properties 
  # SMTStrategy.closingRule "patient_smt"
  SimplificationStrategy.splitMode "DONT_SPLIT"

function
  int length(array('a))
  bool inDom(array('a), int)
  array(int) id(int)

  array('a) write(array('a), int, 'a)
  'a read(array('a), int)
  array('a) swap(array('a), int, int)

  bool isPerm(array('a), array('a))
  bool isPermN(array(int))
  bool isSorted(array(int))
  int count(array('a), 'a)

(*
 * definitions ... rules are axioms
 *)

 axiom length_nonneg
  (\T_all 'a; (\forall x as array('a); length(x) >= 0))
 
rule read_write
  find read(write(%a, %i, %v), %j)
  replace cond(%i=%j, %v, read(%a, %j))
  tags asAxiom

rule swap_def
  find swap(%a, %i, %j)
  replace write(write(%a, %i, read(%a, %j)), %j, read(%a, %i))
  tags asAxiom

rule length_write
  find length(write(%a, %i, %v))
  replace length(%a)
  tags asAxiom

rule length_id
  assume %n >= 0 |-
  find length(id(%n))
  replace %n
  tags asAxiom

rule inDom_def
  find inDom(%a, %i)
  replace 1 <= %i & %i <= length(%a)
  tags asAxiom

rule isPermN_def
  find isPermN(%p)
  replace (\forall i; inDom(%p, i) -> 
      (\exists j; inDom(%p, j) & read(%p, i) = j))
#  tags asAxiom

rule isPerm_def
  find isPerm(%a, %b)
  replace length(%a) = length(%b) 
    & (\exists p; isPermN(p) & length(p) = length(%a)
       & (\forall i; inDom(p, i) ->
            (\exists j; inDom(p, j) & read(%a, i) = read(%b, read(p, j)))))
#  tags asAxiom

rule id_def
  find read(id(%n), %i)
  replace %i
  tags asAxiom

axiom isSorted_def
 (\forall a; isSorted(a) =
   (\forall i; inDom(a, i) -> 
     (\forall j; inDom(a, j) & i < j -> read(a, i) <= read(a, j))))

rule isSorted_def
  find isSorted(%a)
  where freshVar %i, %a
  where freshVar %j, %a
  replace (\forall %i; inDom(%a, %i) -> 
     (\forall %j; inDom(%a, %j) & %i < %j -> read(%a, %i) <= read(%a, %j)))
#  tags asAxiom

(*
 * derived simplification rules
 *)

#proven
rule read_swap
  find read(swap(%a, %i, %j), %k)
  replace cond(%i=%k, read(%a, %j), 
            cond(%j=%k, read(%a, %i), read(%a, %k)))
  tags derived
       rewrite "fol simp"

#proven
rule length_swap
  find length(swap(%a, %i, %j))
  replace length(%a)
  tags derived
       rewrite "fol simp"

# proven
rule inDom_swap
  find inDom(swap(%a, %i, %j), %k)
  replace inDom(%a, %k)
  tags derived
       rewrite "fol simp"

# proven
rule isPermN_read_preserves_inDom
  assume isPermN(%x) |-
  find |- inDom(%x, read(%x,%i))
  replace inDom(%x, %i)
  tags derived

rule isPermN_swap
  find |- isPermN(swap(%a, %i, %j))
  replace isPermN(%a) & inDom(%a, %i) & inDom(%a, %j)
  tags derived

rule isPerm_swap
  find isPerm(swap(%a, %i, %j), %b)
  replace isPerm(%a, %b)
  tags derived
       rewrite "fol simp"

rule isPerm_swap2
  find isPerm(%b, swap(%a, %i, %j))
  replace isPerm(%b, %a)
  tags derived
       rewrite "fol simp"

rule isPerm_id
  find isPermN(id(%n))
  replace true
  tags rewrite "concrete"


rule isPerm_trans
  assume isPerm(%a,%b) |-
  find isPerm(%b,%c) |-
  add isPerm(%a,%c) |-
  tags asAxiom derived

rule isPerm_symm
  find isPerm(%b,%a)
  replace isPerm(%a,%b)
  tags asAxiom derived


rule isPerm_refl
  assume %a = %b |-
  find isPerm(%a, %b)
  replace true
  tags # derived
       rewrite "fol simp"  

rule isPerm_refl2
  find isPerm(%a, %a)
  replace true
  tags rewrite "fol simp"

problem P1:
 (\forall x; isPermN(x) -> (\forall i; inDom(x,i) -> inDom(x,read(x,i))))

problem Empty:
 false
