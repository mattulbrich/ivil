procedure T()
{
	var x : <a,b>[a,b]b;
	var y : <b,a>[a,b]b;
	var z : <a,b>[b,a]a;
	
	x[0,0] := y[0,0];
	z[0,0] := x[0,0];
	
	assert y[0,0] == z[0,0];
	assert (forall i:int, b:bool :: x[i,b] == true) ==> x[42,false];
}
