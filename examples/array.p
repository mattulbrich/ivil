
include "$map.p"
include "$int.p"

function
  int          len(map(int, 'b))
  map('a,'b)   swap(map('a,'b), 'a, 'a)
  map(int,'b)  append(map(int, 'b), 'b)
  map(int,'b)  emptyArray
  map(int,'b)  removeNo(map(int, 'b), int)
  bool         isPerm(map(int, 'b), map(int, 'b))
  bool         isPermN(map(int, int))
  map(int,int) idPerm(int)
  
rule read_swap
  find read(swap(%m, %x, %y), %a)
  replace read(%m, cond(%x=%a, %y, cond(%y=%a, %x, %a)))

rule append_to_write
  find append(%m, %b)
  replace write(%m, len(%m), %b)

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

rule len_append
  find len(append(%m, %b))
  replace len(%m) + 1

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
  replace read(%m, cond(%i < %n, %i, %i - 1))
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
            (\exists j; 0 <= j & j < len(p) & read(%a, i) = read(%b, read(p, j)))))

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
  