
# Factorial using invariants

properties

  # possible values are: SPLIT, DONT_SPLIT, SPLIT_NO_PROGRAMS
  de.uka.iti.pseudo.auto.strategy.SimplificationStrategy.splitMode "SPLIT_NO_PROGRAMS"
  
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.obeyProgramBreakpoints "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.obeySourceBreakpoints "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.stopAtSkip "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.stopAtLoop "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.stopAtJumpBack "false"
  
  de.uka.iti.pseudo.auto.strategy.CompoundStrategy.strategies 
    "de.uka.iti.pseudo.auto.strategy.SimplificationStrategy,
     de.uka.iti.pseudo.auto.strategy.BreakpointStrategy,
     de.uka.iti.pseudo.auto.strategy.SMTStrategy"
  
include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

function
  int c assignable
  int n assignable

  int n_pre
  int fak(int)

axiom fak_def
  fak(0) = 1 &
  (\forall n; n>0 -> fak(n)=fak(n-1)*n)

rule fak_0
  find fak(0)
  replace 1
  tags rewrite "concrete"

rule fak_ind
  find fak(%n)
  assume %n > 0 |-
  replace %n * fak(%n-1)
  tags rewrite "fol simp"
    
program P source "fakultaet.pseudo"
      assume n_pre = n
    sourceline 7
      assume n > 0

    sourceline 9
      c := 1

    sourceline 11
  L1: skip_loopinv fak(n_pre) = c * fak(n) & n >= 0
      goto L2, L6
  L2: assume n > 0
    sourceline 12
      c := c * n
    sourceline 13
      n := n - 1
      goto L1

    sourceline 16
  L6: assume !n> 0
      assert c = fak(n_pre)

problem
  [0;P]true

