/*
  This example is used to test for inverse extensionality, i.e.
  ∃z. x[z] != y[z] -> x!=y;
  
  As this property can be derived from consistency of map_load, it should be
  provable automatically without any additional rules.
*/

procedure P(x:[int]int, y:[int]int)
{
	assume (exists z:int :: x[z] != y[z]);
	assert x != y;
}

procedure Q(x:[int]int, y:[int]int)
{
	// wont work as they might refer to the same map
	assert x != y;
}
