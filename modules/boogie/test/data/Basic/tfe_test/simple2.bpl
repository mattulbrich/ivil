/*
  Very simple example to test lowering phases. This should not be closable.
*/

var _G : int;

procedure P(){
    var x: int;
    x:= 0;
    assert _G == 0; // we dont know anything about _G
}

procedure Q(){
    var x: int;
    x:= 42;
    assert x == 23; // ahh, no
}
