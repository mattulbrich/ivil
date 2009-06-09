include
  "$dynamic.p"
  "$int.p"

sort
  array('component_type)
  elem

function
  'a R(array('a), int) 
  array('a) W(array('a), int, 'a)
  
  bool lessThanOrEqual(elem, elem) infix <<= 70 

  array(elem)  a   assignable
  int          i   assignable
  int          j   assignable
  elem         tmp assignable
  int          N

rule theory_of_arrays
  find { R(W(%a, %i, %v), %j) }
  replace { cond(%i = %j, %v, R(%a,%i)) }

rule ___BESCHISS___
  find { %b }
  closegoal

problem
{
     N >= 0
 ->
     [ i := 0;
       while i < N
       inv
         (\forall x; (\forall y; 0<=x & x<y & y<i -> R(a,x) <<= R(a,y))) & 0<=i & i<=N
       do
         j := i+1;
         while j < N
         inv
           (\forall l; l>=i & l<j -> R(a,j) <<= R(a,l)) & i<j & j<=N & 0<=i & i<N
         do
           if R(a,j) <<= R(a,i)
           then
              tmp := R(a,i);          (* tmp := a[i] *)
              a := W(a, i, R(a,j));   (* a[i] := a[j] *)
              a := W(a, j, R(a,i))    (* a[j] := a[i] *)
           end;
           j := j+1
         end;
         i := i+1
       end
     ] (\forall x; (\forall y; 0<=x & x<y & y<N -> R(a,x) <<=  R(a,y)))
}
