/*
  This examples shows typeerrors in map updates.
*/

procedure P ()
{
	var x: [int]int;
	x := x[5:=5];			//ok
	x := x[:=5]; 			//missing argument 
	x := x[:=true];			//missing argument, wrong type
	x := x[true:=true];		//wrong type
}
