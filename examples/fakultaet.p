include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

function
  int c assignable
  int n assignable
  

program P
  L1: goto L2, L6
  L2: assume n > 0
      c := c * n
      n := n - 1
      goto L1
  L6: assume !n> 0
      assert c = 6

problem
  n=3 & c=1 -> [0;P]

