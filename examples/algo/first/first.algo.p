# Automatically created on Tue Jun 12 18:12:30 CEST 2012
function int $algoMark assignable
include "$int.p"
include "$symbex.p"
include "$decproc.p"
include "jbc/First.first(I)I-1.p"

  function int isum(int)
  axiom isum_def
    (\forall n; isum(n) ~~> isum(n) = cond(n <= 0, 0, n + isum(n-1)))


function int n 
function int sum assignable
function int i assignable

program first source "examples/algo/first/first.algo"
 sourceline 20
  assume (n >= 0) ; "by requirement"
 sourceline 25
  i := 1
 sourceline 26
  sum := 0
 loop0:
 sourceline 29
 sourceline 27
  goto body0, after0
 body0:
  assume (i <= n); "assume condition "
 sourceline 31
  $algoMark := 1 ; "marking stone 1"
  skip_mark ((i + 1) = (i + 1)) ; "marking stone"
 sourceline 32
  sum := (sum + i)
 sourceline 33
  i := (i + 1)
  goto loop0
 sourceline 27
 after0:
  assume $not((i <= n))
 endOfProgram: 


problem true |- [0; first][<0;Java>]true
