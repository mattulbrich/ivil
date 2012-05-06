include "$base.p"

plugin
  metaFunction : "de.uka.iti.pseudo.rule.meta.IncPrgMetaFunction"
  proofHint : "de.uka.iti.pseudo.auto.strategy.hint.MockProofHint"
  
rule oops closegoal

rule assertion
  find |- [%a: assert %b]%phi
  samegoal replace %b
  samegoal replace %b
  samegoal replace $$incPrg(%a)
  tags hintsOnBranches "0, 1"

program P
  assert 1=1 ; "proof using §mock"
  end

problem [0;P]true