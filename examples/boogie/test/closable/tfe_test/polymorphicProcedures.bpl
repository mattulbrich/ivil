/*
  This examples describes a bug that occured when using types, that are defined at the end of the file.
*/

var z : M int;

procedure P <a>(x: a)
{
    var y: M a;
    	
    y[x] := 0;
	
    assert y[x] == 0;
}

type M _ = [_]int;
