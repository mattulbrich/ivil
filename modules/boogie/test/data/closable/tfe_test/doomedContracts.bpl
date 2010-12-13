/*
  The following code should be provable, no matter what die is, as neither A nor
  B will terminate, thus their postconditions trivialy hold(as specified in
  Boogie 2).
*/

// needed to ensure A and B terminate
var x:int;

var die:bool where die==false;

procedure A()
modifies x;
requires x == 1;
ensures x == 2;
{
	x:=1;
	call A();
	if(die){x:=3;}
}

procedure B()
modifies x;
requires x == 1;
ensures x == 2;
{
	x:=1;
	call A();
	if(die){x:=3;}
}
