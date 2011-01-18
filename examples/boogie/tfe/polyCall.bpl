/*
  Tests call of polymorphic procedures.
*/

procedure T()
{
  var i:int;
  call i:=P(0);
}

procedure P <a>(x: a) returns (z:a)
{
    var y: M a;
	
    y[x] := 0;
	
    assert y[x] == 0;
}

type M _ = [_]int;
