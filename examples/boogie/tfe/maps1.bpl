/*
  Tests propper treatment of arrays and templates.
*/

var z : M int;

procedure P ()
{
	var x: [M int, int, bool]int;
    var y: [[int]int, int, bool]int;
    	
    x := y;
	
    assert x == y;
}

type M _ = [_]int;
