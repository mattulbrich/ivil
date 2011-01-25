/*
  Very simple example to test modifies checking.
*/

var y: int;

procedure P() returns (x:int)
ensures x==y;

implementation P() returns(x:int){
	x:=y;
}

implementation P() returns(x:int){
	y:=x; // wrong way, we cant modify y
}
