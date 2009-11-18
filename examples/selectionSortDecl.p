# needed by selectionSort.p

include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

sort 
  array('type)

function
  int length(array('a))
  bool inDom(array('a), int)
  array(int) id

  array('a) write(array('a), int, 'a)
  'a read(array('a), int)
  array('a) swap(array('a), int, int)

  bool isPerm(array('a), array('a))
  bool isPermN(array(int), int)
  bool isSorted(array(int))
  int count(array('a), 'a)

(*
 * definitions
 *)

rule read_write
  find read(write(%a, %i, %v), %j)
  replace cond(%i=%j, %v, read(%a, %j))

rule swap_def
  find swap(%a, %i, %j)
  replace write(write(%a, %i, read(%a, %j)), %j, read(%a, %i))

rule length_write
  find length(write(%a, %i, %v))
  replace length(%a)

rule inDom_def
  find inDom(%a, %i)
  replace 1 <= %i & %i <= length(%a)

rule isPermN_def
  find isPermN(%p, %n)
  replace (\forall i; inDom(%p, i) -> 
      (\exists j; inDom(%p, j) & read(%p, i) = j))

rule isPerm_def
  find isPerm(%a, %b)
  replace length(%a) = length(%b) 
    & (\exists p; isPermN(p,length(%a))
       & (\forall i; inDom(p, i) ->
            (\exists j; inDom(p, j) & read(%a, i) = read(%b, read(p, j)))))
rule id_def
  find read(id, %i)
  replace %i

rule isSorted_def
  find isSorted(%a)
  replace (\forall i; inDom(%a, i) -> 
     (\forall j; inDom(%a, j) & i < j -> read(%a, i) <= read(%a, j)))

(*
 * derived simplification rules
 *)

rule read_swap
  find read(swap(%a, %i, %j), %k)
  replace cond(%i=%k, read(%a, %j), 
            cond(%j=%k, read(%a, %i), read(%a, %k)))
  tags derived
       rewrite "fol simp"

rule length_swap
  find length(swap(%a, %i, %j))
  replace length(%a)
  tags derived
       rewrite "fol simp"

rule isPerm_swap
  find isPerm(swap(%a, %i, %j), %b)
  replace isPerm(%a, %b)
  tags derived
       rewrite "fol simp"

rule isPerm_refl
  assume %a = %b |-
  find isPerm(%a, %b)
  replace true
  tags derived
       rewrite "fol simp"  