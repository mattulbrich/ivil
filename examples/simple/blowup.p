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
  BreakpointStrategy.stopAtLoop "false"
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,SMTStrategy"

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

lemma problem [0;P]true
