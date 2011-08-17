/*
  This examples shows partial template definitions. It should be closable.
*/

type S _ _, T a = S int a, R = T bool;

type A _ _ _, B a = A int a int, C = B bool;

procedure P ()
{
	var x: R,z: S int bool, a:A int bool int, b: C;
	x := z;
	a := b;
}
