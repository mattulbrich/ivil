/*
  Tests naming of in parameters.
*/

procedure P (x: int, y:int)
requires x != 0;
ensures x != 0;
{
    var inner : int;
    
    assert x!=y || x == y;
}

procedure Q (x: int, y:int)
{
    var inner : int;
    
    assert x!=y || x == y;
}

implementation P(y: int, x:int){
   //x := 1; //error you cant assign values to arguments
}
