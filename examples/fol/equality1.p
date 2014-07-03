include "$fol.p"

properties 
  CompoundStrategy.strategies "SimplificationStrategy,BreakpointStrategy,KnowledgeStrategy"

sort
  S

function
  S a
  S b
  S c
  S f(S)

properties
  order.a "10"
  order.b "20"
  order.c "20"

axiom rule directEq
  find %a = %b
  where
    unorderedTerms %a, %b
  replace %b = %a
  tags
    rewrite "fol simp"

lemma problem
  a=b & b=c -> f(b)=f(a) & f(c)=f(b)
