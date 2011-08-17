type T = int;
type S _ = [T]_;
type M = S T;

procedure T()
{
	var x:M;
	var y:[int]int where x == y;
	var z:S int;
    assert x[5] == y[5];
    
    assert x != z ==> y != z;
}
