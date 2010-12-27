/*
  Tests translation of wildcards.
*/


procedure While () returns (x:int)
ensures x == 0;
{  
  while(*){
    x := 1;
    break;
  }
  x := 0;
}    


procedure If () returns (x:int)
ensures x == 0 || x == 1;
{
  x := 2;
  
  if(*){
    x := 1;
  }else{
    x := 0;
  }
}
