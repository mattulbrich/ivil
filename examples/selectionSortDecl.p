# needed by selectionSort.p

include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

sort 
  array('type)

plugin
  prettyPrinter : "test.ArrayPrettyPrinter"

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
 * definitions ... axioms
 *)

axiom array_theory
  (\T_all 'a; (\forall a as array('a); (\forall i as int;
    (\forall j as int; (\forall v as 'a;
    read(write(a, i, v), j) = cond(i=j, v, read(a, j)))))))

axiom write_length
  (\T_all 'a; (\forall a as array('a); (\forall i; (\forall v;
    length(write(a,i,v)) = length(a)))))

axiom id_def
  (\forall i as int; (\forall n as int;
      read(id(n), i) = i))

axiom id_len
  (\forall n; length(id(n)) = n)

axiom swap_def
  (\T_all 'a; (\forall a as array('a); (\forall  i; (\forall j; 
    swap(a,i,j) = write(write(a, i, read(a, j)), j, read(a,i))))))

(*
 * direct consequences as taclets
 *)

rule read_write
  find read(write(%a, %i, %v), %j)
  replace cond(%i=%j, %v, read(%a, %j))
  tags derived

rule swap_def
  find swap(%a, %i, %j)
  replace write(write(%a, %i, read(%a, %j)), %j, read(%a, %i))
  tags derived

rule length_write
  find length(write(%a, %i, %v))
  replace length(%a)
  tags derived

rule length_id
  find length(id(%n))
  replace %n
  tags derived
  
rule inDom_def
  find inDom(%a, %i)
  replace 1 <= %i & %i <= length(%a)

rule isPermN_def
  find isPermN(%p)
  replace (\forall i; inDom(%p, i) -> 
      (\exists j; inDom(%p, j) & read(%p, i) = j))

rule isPerm_def
  find isPerm(%a, %b)
  replace length(%a) = length(%b) 
    & (\exists p; isPermN(p) & length(p) = length(%a)
       & (\forall i; inDom(p, i) ->
            (\exists j; inDom(p, j) & read(%a, i) = read(%b, read(p, j)))))

rule id_def
  find read(id(%n), %i)
  replace %i

rule isSorted_def
  find isSorted(%a)
  replace (\forall i; inDom(%a, i) -> 
     (\forall j; inDom(%a, j) & i < j -> read(%a, i) <= read(%a, j)))

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

rule copy_left
  find %b |-
  add %b |-

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

rule isPerm_id
  find isPermN(id(%n))
  replace true
  tags rewrite "concrete"

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

problem

(\forall x; isPermN(x) -> (\forall i; inDom(x,i) -> inDom(x,read(x,i))))

