include
  "$symbex.p"
  "$int.p"


function
  int val assignable

  bool b1
  bool b2
  bool p(int)

program

  source "pre: val != 0"
           assume !val = 0

  source "if b1"
           goto then1, else1
  source "then"
  then1:   assume b1
           val := 7
           goto after1
  source "else"
  else1:   assume !b1
           val := 9
           goto after1
  after1:  assert val >0


problem 
  [0]

