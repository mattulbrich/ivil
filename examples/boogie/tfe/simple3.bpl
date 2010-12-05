/*
  Very simple example to test lowering phases. This should be closable.
*/

var _G : int;

procedure P()
requires _G == 1;
{
    var x: int;
    var y: bool;
    x:= 0;
    y:= if true then x > 0 else x <= 0;
    
    assume !y; // true
    
    assert _G == 1; // will be true, when requires is implemented
    
    assert (((_G * 2) / 2) - 1 == x) != y <==> true; // should be provable
}
