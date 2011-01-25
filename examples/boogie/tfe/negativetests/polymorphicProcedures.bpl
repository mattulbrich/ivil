/*
  This examples was written to find a bug that occured when using types, that are defined at the end of the file.
*/


procedure P <a>(x: a)
{
    var y: M a;
	
    y[x] := 0;
	
    assert y[x] == 0;
}

type M _ = [_]int;
