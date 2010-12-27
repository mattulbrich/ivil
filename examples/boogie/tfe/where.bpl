/*
  Tests translation of wheres.
*/

var g : int where g == 0;

procedure Where () returns (x:int where x == g)
ensures x == 0;
{
}    

