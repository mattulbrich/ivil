include
  "$dynamic.p"
  "$int.p"

function
  int          i   assignable
  int          j   assignable
  int          tmp assignable

rule ___BESCHISS___
  find { %b }
  closegoal

problem
{
 [ if i > j
   then
      tmp := i;
      i := j;
      j := tmp
   end ] (j >= i)
}
