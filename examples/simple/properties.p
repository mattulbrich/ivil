include
  "$proposition.p"
  "$int.p"
  "$decproc.p"
 
properties
  you.can.define.anything.you.want "surrounded by"
  for.example.you.can.change "split mode:"
  
  SimplificationStrategy.splitMode "SPLIT"
  
  BreakpointStrategy.obeyProgramBreakpoints "false"
  BreakpointStrategy.obeySourceBreakpoints "false"
  BreakpointStrategy.stopAtSkip "false"
  BreakpointStrategy.stopAtLoop "false"
  BreakpointStrategy.stopAtJumpBack "true"
  
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,SMTStrategy"
  
problem
  cond(true, 3, 2) > 2
