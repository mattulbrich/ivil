/*
  Tests propper treatment of arrays and templates.
*/

procedure P ()
{
	var x: [int][int][int]int;
	var y: [int]int;
	
	y[0] := 2;
	x[0][1][3] := 2;
	
    assert x[0][1][3] == y[0];
}
