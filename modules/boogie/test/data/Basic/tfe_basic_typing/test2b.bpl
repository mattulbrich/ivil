type T = S;
type S = T; // should raise a type system exception here

procedure T()
{
	var x : T int;
	var y : T bool;
}
