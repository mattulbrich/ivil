/*
  test propper tranlation of parallel assignments.
*/

var x,y:int where x!=y;

procedure test()
modifies x,y;
ensures x == old(y) && y == old(x);
ensures x != y;
{
	x, y := y, x;
}
