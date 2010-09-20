#
# A small example to artificially blow up a proof tree.
#
# The below program generates exp(x) many branches which all
# end in the assertion.
#
# The label double serves as duplication point.
#
# This example can be used to explore behaviour for large
# and very large proof trees.

properties
<<<<<<< HEAD
  BreakpointStrategy.stopAtJumpBack "false"
  BreakpointStrategy.stopAtLoop "false"
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,SMTStrategy"
=======
  BreakpointStrategy.stopAtLoop "false"
>>>>>>> a35e98d29d18a3b458f110c8a2777a718a4ac501

include "$symbex.p" "$int.p" "$decproc.p"

function int x assignable

program P
  x := 8
 loop:
  goto double, double
 double:
  assume x > 0
  x := x - 1
  goto loop, after

 after:
  assert (\forall x; x+1 = 1+x)

problem [0;P]
