/*
  Tests translation of wheres.
*/

var x,y : int where x == y+1;

procedure Where ()
ensures x == y;
modifies y;
{
  y := y+1;
}    

