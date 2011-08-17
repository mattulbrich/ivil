/*
  Tests inference implementation in ivil. This should be closable.
*/

type ref _, T = <a>[ref a, a]a;

procedure P()
{
	var x:ref int;
	var z:T;
	var y:int;
	
	y := (lambda <a> q:ref a, rval:a:: rval)[x, 0];
	
	assert y == 0;
}
