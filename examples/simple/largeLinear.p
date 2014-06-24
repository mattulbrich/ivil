
#
# Small test case to ensure that a most simple loop can 
# be unrolled 300 times w/o problem .
#

properties
  BreakpointStrategy.stopAtLoop "false"

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

lemma problem 
  [0;T]true
