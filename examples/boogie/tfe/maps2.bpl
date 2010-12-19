/*
  Tests propper treatment of arrays and templates.
*/

procedure P ()
{
	var x: [int]int;
	
    assert (exists y:int :: x[0] == y);
}
