/*
  Tests inference implementation in ivil. This should be closable.
*/

type ref _, T = <a>[ref a]a;

procedure P()
{
	var x:ref int;
	var z:T;
	var y:int;
	
	y := z[x];
	
	assert y == z[x];
}
