/*
  Tests performance of lambda evaluation.
*/

procedure P ()
{
	var x: [int]int;
	
	x := (lambda x:int :: x/2);
	
    assert x[3] == x[2];
    assert (lambda x:int, b:bool :: if b then x else -x)[-1,false] == 1;
    assert (lambda x:int :: (lambda b:bool :: if b then x else -x))[-1][false] == 1;
}
