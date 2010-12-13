/*
  Very simple example to test goto and return.
*/

procedure P(x:int)
requires x!=0;
ensures x > 0;
{
    goto A,B;
	
  A:
    assume x > 0;
    return;
  B:
    assume x < 0;
    x := -x;
    return;
    
    //unreachable
    assert false;
}
