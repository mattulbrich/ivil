/*
  This should be provable.
*/

procedure P()returns()
{
	assert 6 == |{ main: return 6; }|;
}
