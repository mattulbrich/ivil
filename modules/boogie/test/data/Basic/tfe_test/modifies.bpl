/*
  Very simple example to test polymorphic procedures
*/

var y: int;

procedure P() returns (x:int)
ensures x==y;

implementation P() returns(x:int){
	x:=y
}

implementation P() returns(x:int){
	y:=x // wrong way, we aint modify y
}
