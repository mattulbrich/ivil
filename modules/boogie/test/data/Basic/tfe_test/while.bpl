/*
  Very simple example to test while loops.
*/

procedure P(x:int)
requires x > 0;
{
    var i: int;
    i:= 0;
    while(true){
    	if(x == i){
    		break;		
    	}
    	i := i + 1;
    }
    assert x == i;
}
