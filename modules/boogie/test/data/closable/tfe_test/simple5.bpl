/*
  Very simple example to test lowering of functions.
*/

function f(int) returns (bool);
function g(x:int) returns (bool) { f(x) }

procedure P()
{
    assert (forall x:int :: f(x) == g(x));
}
