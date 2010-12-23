/*
  Tests propper treatment of arrays and templates.
*/

procedure P ()
{
	var x: [int]int;
	
	x[2] := x[3];
	
    assert x[3] == x[2];
}
