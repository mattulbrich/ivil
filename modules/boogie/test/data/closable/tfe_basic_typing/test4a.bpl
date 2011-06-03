procedure T()
{
    var x:<a>[a]a;
    var a,o:int where a > o;
    
    x[5] := 3;
    assert x[5] == 3;
    
    assert a > o && !(a <= o) && !(a < o) && a >= o && a!=o;
}
