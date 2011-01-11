/*
  Accessing type arguments.
*/

type pointer _;
function load <a> (pointer a) returns (a);

procedure P()
{
    var p: pointer int;
    var x: int;
    	
    x := load(p);
}
