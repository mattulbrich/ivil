# Automatically created on Wed Dec 14 14:22:50 CET 2011
include "$int.p"
include "$symbex.p"
include "$decproc.p"
function
  int fac(int)

rule fac_0
  find fac(0)
  replace 1
  tags 
    rewrite "concrete"
    asAxiom

rule fac_ind
  find fac(%n)
  assume %n > 0 |-
  replace %n * fac(%n-1)
  tags 
    rewrite "fol simp"
    asAxiom

function int n
function int i assignable
function int c assignable

program factorial source "factorial.algo"
 sourceline 43
  i := n 
 sourceline 44
  c := 1 
 loop0:
 sourceline 46
  skip_loopinv fac ( n ) = c * fac ( i ) & i >= 0 , i 
 sourceline 45
  goto body0, after0
 body0:
  assume ( i > 0 ) ; "assume condition "
 sourceline 49
  c := c * i 
 sourceline 50
  i := i - 1 
  goto loop0
 sourceline 45
 after0:
  assume $not(( i > 0 ) )


problem 
n > 0  |- [[0;factorial]]((c = fac ( n ) ))
