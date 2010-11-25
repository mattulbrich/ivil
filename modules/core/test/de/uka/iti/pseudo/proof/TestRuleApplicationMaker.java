package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

public class TestRuleApplicationMaker extends TestCaseWithEnv {
    
    private Sequent seq;
    private Proof proof;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(seq == null) {
            // b1, i1=i2 |- f(i1)=f(i2) & b1 & $not(b2)), b2
            // b1 $eq(i1,i2) |- $and($and($eq(f(i2),f(i1)),b1),$not(b2)) b2
            seq = new Sequent(new Term[] { makeTerm("b1"), makeTerm("i1=i2") }, 
                new Term[] { makeTerm("(f(i2) = f(i1)) & b1 & !b2"), makeTerm("b2") });
            System.out.println(seq);
        }
        proof = new Proof(seq);
    }

    public void testMatching() throws Exception {
        
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setRule(env.getRule("replace_known_left"));
        ram.setFindSelector(new TermSelector("S.0.0.1"));
        ram.pushAssumptionSelector(new TermSelector("A.0"));
        ram.setProofNode(proof.getRoot());
        ram.matchInstantiations();
        
        System.err.println(ram);
        proof.apply(ram, env);
        
        Term result = proof.getOpenGoals().get(0).getSequent().getSuccedent().get(0);
        assertEquals(makeTerm("(f(i2) = f(i1)) & true & !b2"), result);
    }
    
    public void testMatching2() throws Exception {
        
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setRule(env.getRule("equality_apply"));
        ram.setFindSelector(new TermSelector("S.0.0.0.1.0"));
        ram.pushAssumptionSelector(new TermSelector("A.1"));
        ram.setProofNode(proof.getRoot());
        ram.matchInstantiations();
        
        System.err.println(ram);
        proof.apply(ram, env);
        
        Term result = proof.getOpenGoals().get(0).getSequent().getSuccedent().get(0);
        assertEquals(makeTerm("(f(i2) = f(i2)) & b1 & !b2"), result);
    }
    
}
