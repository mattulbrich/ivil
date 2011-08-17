procedure T()
{
	var x:<a>[a]a;
    
    assert x[true] || (x[0] == 0 ==> !x[true]);
}
