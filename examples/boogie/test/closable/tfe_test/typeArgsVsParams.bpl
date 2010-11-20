/*
  This should create an type error, as S and T are not compatible.
*/

type S = <a>[a]int;
type T _ = [_]int;

procedure P()
{
	var v:<a>[a]int, x:S, y:T int;
	x := (y:T int); // wont work, y:=(x:S) also wont work
	
    v[x] := 0;
	v[y] := 0;
	
    assume (forall <a> z:a :: (z!=x && z!=y ==> v[z] == 1));
    
    assert (forall <a,b> i:a,j:b :: 0 == v[i] && v[j] == 0 ==> i == j);
}
