type T;

procedure T()
{
	var x:<a>[a]T;
	x[5] := x[3];
    assert x[5] == x[3];
}
