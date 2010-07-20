include
  "$proposition.p"
  "$int.p"
  "$decproc.p"
 
properties
  you.can.define.anything.you.want "surrounded by"
  for.example.you.can.change "split mode:"
  
  de.uka.iti.pseudo.auto.strategy.SimplificationStrategy.splitMode "SPLIT"
  
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.obeyProgramBreakpoints "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.obeySourceBreakpoints "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.stopAtSkip "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.stopAtLoop "false"
  de.uka.iti.pseudo.auto.strategy.BreakpointStrategy.stopAtJumpBack "true"
  
  de.uka.iti.pseudo.auto.strategy.CompoundStrategy.strategies "de.uka.iti.pseudo.auto.strategy.SimplificationStrategy,de.uka.iti.pseudo.auto.strategy.BreakpointStrategy,de.uka.iti.pseudo.auto.strategy.SMTStrategy"
  
problem
  cond(true, 3, 2) > 2
