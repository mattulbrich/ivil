/*
  Tests codeexpressions. These should not be provable.
*/

procedure P ()
{
	var x:bool;
    x:= |{ A: goto A; B: return true; }|; // this would have been rejected by boogie as specified in krml207; ivil doesnt care
}
