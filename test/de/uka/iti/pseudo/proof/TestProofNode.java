/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.Arrays;
import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

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
    
    public void testPrune() throws Exception {
        Term term = makeTerm("$and(b1, b2)");
        Proof p = new Proof(term);
        ProofNode orgRoot = p.getRoot();
        Rule rule = env.getRule("and_right");
        Term pattern = rule.getFindClause().getTerm();
        
        RuleApplicationMaker app = new RuleApplicationMaker(env);        
        app.setRule(rule);
        app.setFindSelector(new TermSelector("S.0"));
        app.getTermUnification().leftUnify(pattern, term);
        
        p.apply(app, env);
        p.prune(orgRoot);
     
        assertEquals(null, orgRoot.getChildren());
    }
    
    public void testPruneClosed() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term);
        ProofNode orgRoot = p.getRoot();
        Rule rule = env.getRule("close_true_right");
        
        RuleApplicationMaker app = new RuleApplicationMaker(env);        
        app.setRule(rule);
        app.setFindSelector(new TermSelector("S.0"));
        
        p.apply(app, env);
        assertEquals(Collections.emptyList(), orgRoot.getChildren());
        
        p.prune(orgRoot);
        assertEquals(null, orgRoot.getChildren());
    }
    
    public void testPruneWrongProof() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term);
        Proof p2 = new Proof(term);
        
        try {
            p.prune(p2.getRoot());
            fail("Should have failed: Wrong proof for node");
        } catch (ProofException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testIllegalGoalNumber() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term);
        Rule rule = env.getRule("close_true_right");
        
        RuleApplicationMaker app = new RuleApplicationMaker(env);        
        app.setRule(rule);
        app.setGoalNumber(1);
        app.setFindSelector(new TermSelector("S.0"));
        
        try {
            p.apply(app, env);
        } catch (ProofException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testApplyTwice() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term);
        Rule rule = env.getRule("close_true_right");
        RuleApplicationMaker app = new RuleApplicationMaker(env);        
        app.setRule(rule);
        app.setGoalNumber(0);
        app.setFindSelector(new TermSelector("S.0"));
        p.apply(app, env);
        
        try {
            p.getRoot().apply(app, env);
            fail("should have failed: applied twice to a proof node");
        } catch (ProofException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testRemovingRule() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term);
        Rule rule = env.getRule("remove_right");
        RuleApplicationMaker app = new RuleApplicationMaker(env);        
        app.setRule(rule);
        app.setGoalNumber(0);
        app.setFindSelector(new TermSelector("S.0"));
        app.getTermUnification().addInstantiation((SchemaVariable) makeTerm("%a as bool"), makeTerm("true"));
        
        p.apply(app, env);
        
        Sequent s = p.getGoal(0).getSequent();
        
        assertEquals(0, s.getAntecedent().size());
        assertEquals(0, s.getSuccedent().size());
    }
}
