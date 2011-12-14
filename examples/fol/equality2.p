include "$int.p" "$fol.p"

properties 
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,KnowledgeStrategy"
  order.f "100"

function
  int a
  int b
  int f(int)
  bool c
  bool d

problem
  a=b & b = f(a) -> a = f(f(f(a)))

  
