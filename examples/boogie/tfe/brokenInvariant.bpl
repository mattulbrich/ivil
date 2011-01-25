/*
  An implementation of factorial using recursion and iteration.
  
  Both are provable with boogie.
*/

function fac(x :int):int { if x > 1 then x * fac(x-1) else 1 }

procedure recFac(x :int) returns (f :int)
ensures f == fac(x);
{
  if(x <= 1) {
    f := 1;
  } else {
	call f := recFac(x-1);
	f := f * x;
  }
}


procedure itFac(x :int) returns (f :int)
ensures f == fac(x);
{
  var i :int where i == 1, r :int where r == x;
  f := 1;
  while(i<=x)
  invariant f*fac(r) == fac(x);
  {
    f := f * r;
    r := r - 1;
    // broken: i := i + 1;
  }
}
