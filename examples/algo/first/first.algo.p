# Automatically created on Fri Jun 08 14:05:49 CEST 2012
include "$int.p"
include "$symbex.p"
include "$decproc.p"

  function int isum(int)
  axiom isum_def
    (\forall n; isum(n) ~~> isum(n) = cond(n <= 0, 0, n + isum(n-1)))


function int n 
function int sum assignable
function int i assignable

program first source "first.algo"
 sourceline 19
  assume (n >= 0) ; "by requirement"
 sourceline 24
  i := 1
 sourceline 25
  sum := 0
 loop0:
 sourceline 28
  skip_loopinv ((sum = isum((i - 1))) & ((1 <= i) & (i <= (n + 1)))), ((n - i) + 1)
 sourceline 26
  goto body0, after0
 body0:
  assume (i <= n); "assume condition "
 sourceline 30
  sum := (sum + i)
 sourceline 31
  i := (i + 1)
  goto loop0
 sourceline 26
 after0:
  assume $not((i <= n))
 endOfProgram: 
 sourceline 21
  assert (sum = isum(n)) ; "by ensures"

