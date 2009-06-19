(*
 * Experiments with arrays
 *
 * Lets do only int arrays first.
 *  -- ignore null pointers first
 *  -- ignore lengths first
 * check negative indices however!
 *)

include
  "$symbex.p"
  "$int.p"

sort
  heap
  array

function
  int R(heap, array, int) 
  heap W(heap, array, int, int)
  heap         H0

(* Program variables *)
  heap         H   assignable
  array        a   assignable
  array        b   assignable
  int          i   assignable
  int          j   assignable
  int          tmp assignable
  int          N

rule theory_of_arrays
  find  R(W(%h, %a, %i, %v), %b, %j) 
  replace cond(%i = %j & %a = %b, %v, R(%h, %a, %j))
  tags rewrite "fol simp"

program
    
  source "pre: N = 1"
    assume H0 = H
    assume N = 1
  source "for(int i = 0; i < N; i++) {"
    i := 0
   loopCondition:
    goto loopBody, afterLoop
   loopBody:
    assume i < N    
  source "  int tmp = a[i];"
    assert i >= 0
    tmp := R(H, a, i)
  source "  a[i] = b[i];"
    assert i >= 0
    H := W(H, a, i, R(H, b, i))
  source "  b[i] = tmp;"
    assert i >= 0
    H := W(H, b, i, tmp)
  source "}"
    i := i + 1
    goto loopCondition
  source "post: \forall i in [0..N] ; a[i]=\old(b[i]) & b[i] = \old(a[i])"
   afterLoop:
    assume !(i <N)
    end (\forall x; x>=0 & x < N -> R(H,a,i)=R(H0,b,i) & R(H,b,i) = R(H0,a,i))

problem [0]

