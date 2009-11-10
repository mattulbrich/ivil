package de.uka.iti.pseudo.proof;

import java.util.Arrays;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

public class TestProofNode extends TestCaseWithEnv {
    
    // from a bug
    public void testExistsRight() throws Exception {
        Binding term = (Binding) makeTerm("(\\exists i; i>0)");
        Rule rule = env.getRule("exists_right");
        Term pattern = rule.getFindClause().getTerm();
        Proof p = new Proof(term);
        
        RuleApplicationMaker app = new RuleApplicationMaker(env);
        
        app.setRule(rule);
        app.setFindSelector(new TermSelector("S.0"));
        app.setGoalNumber(0);
        app.getTermUnification().leftUnify(pattern, term);
        app.getTermUnification().addInstantiation((SchemaVariable) makeTerm("%inst as int"), makeTerm("3"));
        
        p.apply(app, env);
        
        Term result = p.getGoal(0).getSequent().getSuccedent().get(1);
        
        assertEquals(makeTerm("3>0"), result);
    }

    // from a bug
    public void testForallLeft() throws Exception {
        Binding term = (Binding) makeTerm("(\\forall i; i>0)");
        
        Rule rule = env.getRule("forall_left");
        Term pattern = rule.getFindClause().getTerm();
        Proof p = new Proof(new Sequent(Arrays.<Term>asList(term), Arrays.<Term>asList()));
        
        RuleApplicationMaker app = new RuleApplicationMaker(env);
        
        app.setRule(rule);
        app.setFindSelector(new TermSelector("A.0"));
        app.setGoalNumber(0);
        app.getTermUnification().leftUnify(pattern, term);
        app.getTermUnification().addInstantiation((SchemaVariable) makeTerm("%inst as int"), makeTerm("3"));
        
        p.apply(app, env);
        
        Term result = p.getGoal(0).getSequent().getAntecedent().get(1);
        
        assertEquals(makeTerm("3>0"), result);
    }
}
