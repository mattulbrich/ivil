var x:[int]int;

procedure T()
{
	var y:[int]int;
	y := x;
    assert x[y[5]] == y[x[5]];
}
