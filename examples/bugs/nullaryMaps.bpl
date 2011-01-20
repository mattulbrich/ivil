procedure P()
{
  var m: []int;
  
  m[] := 1;

  assert (m)[] == (lambda x:int :: x+1)[0];
}
