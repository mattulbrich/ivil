/*
  Simple test for call forall statement.
*/

// unprovable; not a problem as its only for testing
procedure Lemma(x: int)
  requires x == 1;
  ensures x == 0;
  
// unprovable; not a problem as its only for testing
procedure Lemma2(x: int)
  requires x == 0;
  ensures false;

procedure Main()
{
  var x: int where x == 1;
  call forall Lemma(x);
  call forall Lemma2(x);
  assert false;
}
