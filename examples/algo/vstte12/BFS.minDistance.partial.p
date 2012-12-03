include "BFS.minDistance(int,int).p"

properties
   CompoundStrategy.strategies 
      "SimplificationStrategy,
       BreakpointStrategy,
       KnowledgeStrategy,
       SMTStrategy"
   SimplificationStrategy.splitMode
      "DONT_SPLIT"

problem [0; Java]true 