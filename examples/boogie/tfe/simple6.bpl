/*
  Tests scoping.
*/

type bitvector;

var x : int;
function x(int) returns (int) {0}

procedure p(y:int)
{
    var x :int;
    var r :bitvector;
	assume 0==x(x);
	if(0==x){
	  assert (exists x:int :: 0 == x(x));
	}
}
