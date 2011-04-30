include 
  "$int.p"
  "$base.p"
  "$symbex.p"

function int i assignable

program T
  i := 300
  loop: assume i >= 0
  i := i - 1
  goto loop 

problem [0;T]
