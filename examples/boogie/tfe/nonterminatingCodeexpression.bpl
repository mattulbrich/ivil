/*
  Tests non terminating codeexpressions. Should work fine and be provable.
*/

procedure P ()
{
	var x:bool;
    x:= |{ A: goto A; B: return true; }|; // this would have been rejected by boogie as specified in krml207; ivil doesnt care
}

procedure Q()
{
	var x:bool;
    x:= |{ A: assume false; return true; }|; // this should be ok even in boogie, but also wont terminate
}
