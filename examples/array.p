
include "$map.p"
include "$int.p"

function
  int          len(map(int, 'b))
  map('a,'b)   swap(map('a,'b), 'a, 'a)
  map(int,'b)  insert(map(int,'b), int, 'b)
  map(int,'b)  emptyArray
  map(int,'b)  removeNo(map(int, 'b), int)
  bool         isPerm(map(int, 'b), map(int, 'b))
  bool         isPermN(map(int, int))
  map(int,int) idPerm(int)
  
rule read_swap
  find read(swap(%m, %x, %y), %a)
  replace read(%m, cond(%x=%a, %y, cond(%y=%a, %x, %a)))

rule len_write
  find len(write(%m, %a, %b))
  replace len(%m)
  tags 
    rewrite "fol simp"

rule len_swap
  find len(swap(%m, %x, %y))
  replace len(%m)
  tags 
    rewrite "fol simp"

rule len_insert
  find len(insert(%m, %i, %b))
  replace len(%m) + 1
  tags 
    rewrite "fol simp"

rule len_emptyArray
  find len(emptyArray)
  replace 0
  tags 
    rewrite "fol simp"

rule len_nonneg
  find len(%a)
  add len(%a) >= 0 |-

rule len_removeNo
  find len(removeNo(%m, %n))
  replace len(%m) - 1
  tags 
    rewrite "fol simp"

rule read_removeNo
  find read(removeNo(%m, %n), %i)
  replace read(%m, cond(%i < %n, %i, %i + 1))
  tags 
    rewrite "fol simp"

rule read_insert
  find read(insert(%m, %i, %b), %j)
  replace cond(%j = %i, %b, read(%m, cond(%j < %i, %j, %j-1)))
  tags 
    rewrite "fol simp"  

rule isPerm_swap
  find isPerm(swap(%m, %a, %b), %n)
  replace isPerm(%m, %n)

rule isPerm_def
  find isPerm(%a, %b)
  replace len(%a) = len(%b) 
    & (\exists p; isPermN(p) & len(p) = len(%a)
       & (\forall i; 0 <= i & i < len(p) ->
            read(%a, i) = read(%b, read(p, i))))

rule isPerm_refl
  find isPerm(%a, %a)
  replace true
  tags
    rewrite "fol simp"

rule read_idPerm
  find read(idPerm(%n), %i)
  replace %i
  tags 
    rewrite "fol simp"  

rule len_idPerm
  find len(idPerm(%n))
  replace %n
  tags 
    rewrite "fol simp"

rule isPermN_def
  find isPermN(%p)
  replace (\forall i; 0 <= i & i < len(%p) -> 
      (\exists j; 0 <= j & j < len(%p) & read(%p, i) = j))

rule isPermN_idPerm
  find isPermN(idPerm(%n))
  replace true
  