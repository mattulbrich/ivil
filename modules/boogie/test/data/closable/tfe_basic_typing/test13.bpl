type Map _  = [_]bool;

procedure T()
{
	var x:<a>[a][a]a;
	var y:Map int;

	x[y][y] := y;
	
    assert x[y][y] == y;
}
