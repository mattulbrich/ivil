/*
  Tests translation of bitvector select expressions.
*/

procedure P()
{
  var x : bv32;
  x := 42bv32;
  
  assert x[1:0] == 0bv1;
}
