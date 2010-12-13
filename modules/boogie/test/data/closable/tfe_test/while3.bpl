/*
  Tests treatment of invariants.
*/

function fac(x:int):int { if x < 1 then 1 else x*fac(x) }

procedure P(x:int) returns (rval : int)
ensures rval == fac(x);
{
	var i :int;
	rval := 1;
	i := x;
    while(i > 0)
    invariant fac(i) * rval == fac(x);
    {
    	rval := rval * i;
    	i := i - 1;
    }
}
