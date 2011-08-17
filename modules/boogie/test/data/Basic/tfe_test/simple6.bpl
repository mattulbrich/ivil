/*
  Very simple example to test goto and return.
*/

procedure P(x:int) returns (y:int)
requires x!=0;
ensures y > 0;
{
    goto A,B;
	
  A:
    assume x > 0;
    y := x;
    return;
  B:
    assume x < 0;
    y := -x;
    return;
    
    //unreachable
    assert false;
}
