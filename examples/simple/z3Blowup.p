#
# A small example to artificially blow up a proof tree.
#
# The below program generates exp(x) many branches which all
# end in the assertion.
#
# The label double serves as duplication point.
#
# The assertions can only be discharged by an smt solver
# or manually since the necessary axiom is not applied
# in the automatic

properties
  BreakpointStrategy.stopAtLoop "false"
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy"

include "$symbex.p" "$int.p" "$decproc.p"

function 
  int x assignable
  bool phi

axiom phi_holds
  phi

program P
  x := 9
 loop:
  goto double, double
 double:
  assume x > 0
  x := x - 1
  goto loop, after

 after:
  assert phi

lemma problem 
  [0;P]true
