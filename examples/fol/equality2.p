include "$int.p" "$fol.p"

properties 
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,KnowledgeStrategy"
  order.f "10"

function
  int a
  int b
  int f(int)
  bool c
  bool d

lemma problem
  a=b & b = f(a) -> a = f(f(f(a)))

  
