/*
  tests handling of bound code expressions.
*/
procedure P()
ensures (forall i : int :: i == |{ A: return i; }|);
{
}
