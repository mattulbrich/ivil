/*
  Very simple example to test polymorphic procedures
*/

procedure P()
{
  var x : bv32;
  x := 42bv32;
  
  assert x[1:0] == 0bv1;
}
