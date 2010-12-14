/*
  Very simple example to test polymorphic procedures
*/

procedure P<a>(x:a) returns (y:a)
ensures x==y;
{
    goto A,B;
	
  A:
  	y := x;
    return;
  B:
    assume x==y;
    return;
}

procedure Q<a>(x:a)
{
	assume false;
}
