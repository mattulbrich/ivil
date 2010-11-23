/*
  Tests propper treatment of arrays and templates.
*/

var z : M int;

procedure P ()
{
	var x: <a>[M a]int;
    var y: <c>[[c]int]int;
    	
    x := y;
	
    assert x == y;
}

type M _ = [_]int;
