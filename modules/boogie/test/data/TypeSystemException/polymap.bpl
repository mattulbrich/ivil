/*
  Test polymorphism properties. For some reason this is closable.
  
  Note: Boogie 2 Manual says:
"There is a restriction on polymorphic map types. Each bound type variable must be
mentioned somewhere in the map type’s domain types. For example, α [int]α is not
allowed, because the domain type int does not mention α"

	Maybe this indicates a bug or unsafe change in behavior for boogie.
*/

procedure P()
{
	var x :<a>[int]a, y:<b>[int]b;

	x[1] := true;	
	x[1] := 1;
	y[1] := 1;
	y[1] := false;
	assert x[1] == 1 && x[1] == true && y[1] == x[1];
}
