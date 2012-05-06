include "$base.p" "$symbex.p"

properties
  CompoundStrategy.strategies 
     "HintStrategy,
      SimplificationStrategy,
      BreakpointStrategy,
      SMTStrategy"

rule oops 
   closegoal

program P
  assert false ; "lemma to be proved later §(rule oops)"
  assert false ; "lemma with §(cut '1 = 1')"

problem [0;P]true