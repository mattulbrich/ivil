/*
  Very simple example to test nested while loops.
*/

procedure P()
{
    var x: int;
    x:= 0;
    while(true)
    {
      while(true){
    	x := x + 1;
    	if(x >= 5){
    	  break;		
    	}
      }
      if(x >= 6){
    	break;
      }
    }
    assert x == 6;
}
