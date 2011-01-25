/*
  Shows how to get access to type arguments.
*/

type pointer _;
function load <a> (x: pointer a):a;

var x: int;

procedure P()
{
    var p: pointer int;
    var x: int;
    	
    x := load(p);
}
