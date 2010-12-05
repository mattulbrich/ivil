/*
  Very simple example to test lowering of boolean quantifieres.
*/

var x:int;

procedure P()
requires x==2;
ensures x==1;
{
    //some magic happens, we dont want to express is, so lets just say x will be 1 after that
    havoc x;

    assume (exists z:int :: z==1 && (forall y:int :: y!=z ==> y!=x)); // strange way to say, x == 1
    
    assert true; //should be reachable
}
