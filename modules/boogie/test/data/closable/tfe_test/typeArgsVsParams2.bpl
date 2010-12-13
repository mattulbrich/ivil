/*
  This is a fixed version, that should be closable.
*/

//one try with templates
type S _ = [_]int;
type T _ = [_]int;

procedure P()
{
	var v:<a>[a]int, x:S int, y:T int;
	y := (x:T int);
    v[x] := 0;
	v[y] := 0;
	
    assume (forall <a> z:a :: (z!=x && z!=y ==> v[z] == 1));
    
    assert (forall <a,b> i:a,j:b :: 0 == v[i] && v[j] == 0 ==> i == j);
}

//one try with polymorphism
type U = <m>[m]int;
type V = <a>[a]int;

procedure Q()
{
	var v:<a>[a]int, x:U, y:V;
	y := (x:V);
    v[x] := 0;
	v[y] := 0;
	
    assume (forall <a> z:a :: (z!=x && z!=y ==> v[z] == 1));
    
    assert (forall <a,b> i:a,j:b :: 0 == v[i] && v[j] == 0 ==> i == j);
}
