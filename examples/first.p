include
  "$symbex.p"
  "$int.p"


function
  int val assignable

  bool b1
  bool b2
  bool p(int)

program P source "first.src"

  sourceline 7
           assume !val = 0

  sourceline 9
           goto then1, else1
  sourceline 10
  then1:   assume b1
  sourceline 11
           val := 7
           goto after1
  sourceline 12
  else1:   assume !b1
  sourceline 14
           assert !val = 0
           val := 1/val
  sourceline 15
           val := val + 9
           goto after1
  sourceline 18

  after1:  assert val >0


problem 
  [0; P]true

