/*
  Shows old in procedure body.
*/


var x: int;

procedure P() returns (y:int)
free requires 0 != 1;
modifies x;
ensures y == old(x);
{
	x := 0;
	y := old(x);
}
