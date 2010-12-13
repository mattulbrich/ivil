/*
  Tests for check of nested type parameter shadowing, which is forbidden.
*/

var z : M int;

procedure P <a>(x: a)
{
    var y: <a>[a] a;
    	
    y[x] := 0;
	
    assert y[x] == 0;
}

type M _ = [_]int;
