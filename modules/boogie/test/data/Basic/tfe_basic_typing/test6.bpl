type T _;

procedure T()
{
	var x : T int;
	var y : T bool;
	var u: [T int]int;
	
//	x := y; // should raise a type system exception
	
//	assert 0 != u[y]; // should raise a type system excetpion
}
