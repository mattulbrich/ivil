include
  "$dynamic.p"
  "$int.p"


function
  int val1 assignable
  int val2 assignable
  int val1AtPre

problem
 { 
    val1AtPre = val1 
  & val1 >= 0
  -> 
     [ val2 := 0;
       while val1 > 0
       inv
         val1 + val2 = val1AtPre & val1 >= 0
       do
         val1 := val1-1;
         val2:=val2+1 end ](val2=val1AtPre)

 }
