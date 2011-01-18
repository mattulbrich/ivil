/*
  Test old for called procedures.
*/


var x: int;

procedure T()
modifies x;
ensures x == old(x);
{
}

procedure P() returns (y:int)
modifies x;
ensures y == old(x);
ensures x == 0;
{
	x := 0;
	y := old(x);
	call T();
}
