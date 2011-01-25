/*
  Each system, that contains magic is broken somehow;
  this examples is provable using boogie, but currently unsupported in ivil.
*/

function magic<T>() returns (T);

procedure P() returns(){
	var x:bv32;
	
	x := magic() ++ magic(); // wont work, as polymorphic types currently return bv0
	
}
