/*
  This example shows why global where and call by contract without proof
  management are bad ideas.
  
  This is provable in boogie release and nightly; it will be provable in ivil,
  until a better handling for call is implemented.
*/
var x, y : int where x == y - 1;

procedure doom()
modifies x,y;
ensures x == y;
{
  x := x + 1;
  
  call doomed();
}

procedure doomed()
// requires x == y - 1;  // uncomment this and the proof will fail
modifies x,y;
ensures x == y;
{
  x := x + 1;
}
