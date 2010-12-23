/*
  Tests propper treatment of arrays and templates.
*/

procedure P ()
{
	var x: [int][int]int;
	var y,z: int;
	
	y := 1;
	
	assume y==z;
	
	x[y][z] := 2;
	
	x[y][0] := x[z][0];
	
	x[2] := x[3];
	
    assert x[y] == x[z];
}
