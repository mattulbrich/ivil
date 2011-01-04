/*
  Simple test for call forall statement.
*/

// unprovable; not a problem as its only for testing
procedure Lemma(x: int)
  requires x == 0;
  ensures false;

procedure Main()
{
  var x: int where x == 1;  // change this to 0 and the prove will close
  call forall Lemma(x);
  assert false;
}
