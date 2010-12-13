/*
  Each system, that contains a god is broken somehow;
  this examples is provable using boogie, but currently unsupported in ivil.
*/

function god<T>() returns (T); // fails because T can not be safely inferred, EVER

procedure P() returns(){
	var x:bv32;
	
	x := god() ++ god(); // wont work, as polymorphic types currently return bv0
	
}
