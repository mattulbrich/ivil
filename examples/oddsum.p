(*
 * ivil input file - for the sake of comparison to KeY
 *)

include 
  "$fol.p" 
  "$decproc.p"

sort N

function
  N s(N)
  N O
  N oddsum(N)
  N mult(N,N)
  N plus(N,N)

axiom rule induction_on_naturals
  find |- (\forall %n as N; %phi)
  samegoal "Base Case"
    replace $$subst(%n, O, %phi)
  samegoal "Step Case"
    replace (\forall %n; %phi -> $$subst(%n, s(%n), %phi))

lemma oddsum
  (\forall n; !s(n) = O) &
  (\forall n; (\forall m; s(n) = s(m) -> n = m)) &
  (\forall n; plus(n, O) = n) &
  (\forall n; (\forall m; plus(n, s(m)) = s(plus(n,m)))) &
  (\forall n; mult(n, O) = O) &
  (\forall n; (\forall m; mult(n, s(m)) = plus(mult(n,m),n))) &
  (\forall n; oddsum(s(n)) = plus(oddsum(n), s(plus(n,n)))) &
  oddsum(O) = O&

  (* Lemma proved outside the file *)
  (\forall c; (\forall a; (\forall b;
    plus(plus(a,b), c) = plus(a, plus(b,c))))) &
    
  (* bewiesen in multcomm.key: *)
  (\forall a; (\forall b; mult(a,b) = mult(b,a)))

->
  (\forall x; oddsum(x) = mult(x,x))
