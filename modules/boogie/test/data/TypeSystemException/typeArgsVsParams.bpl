/*
  This should create an type error, as S and T are not compatible.
*/

type S = <a>[a]int;
type T _ = [_]int;

procedure P()
{
	var v:<a>[a]int, x:S, y:T int;
	x := (y:S); // wont work
}
