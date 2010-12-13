/*
  This shows a safe way to realize typecasts. Unfortunately, you have to supply
  the type as an expression of the desired type.
*/

// you have to supply the returntype
function cast <a, T>(a, T) returns (T);


procedure P()
{
    var x :int;
    	
    x := 0;
    
    //define behavior of toBool to be c-style
    assume (forall i :int, T:bool :: cast(i, T) == false <==> i == 0);
	
    assert cast(x, true) == false;
}


// you can also realize cast as a map, which looks much nicer
type cast _ = <a>[a]_;



procedure Q()
{
  var x:int;
  var toBool: cast bool;
  
  //define behavior of toBool to be c-style
  assume (forall i :int :: toBool[i] == false <==> i == 0);
  
  assume x!=0;
  assert toBool[x] == true;
}
