type C;

procedure P(x:int, y:bool) returns (z:C);

procedure CallP()
{
var x: int;
var y: bool;
var z: C;

call z := P(x, y);
call * := P(x, y);
call z := P(*, y);
call z := P(x, *);
call * := P(x, *);
call * := P(*, y);
call z := P(*, *);
call * := P(*, *);
}

