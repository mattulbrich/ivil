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
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Update;

public class TestProofNode extends TestCaseWithEnv {

    // from a bug
    public void testExistsRight() throws Exception {
        Binding term = (Binding) makeTerm("(\\exists i; i>0)");
        Rule rule = env.getRule("exists_right");
        Term pattern = rule.getFindClause().getTerm();
        Proof p = new Proof(term, env);

        assertEquals(1, p.getRoot().getNumber());

        RuleApplicationMaker app = new RuleApplicationMaker(env);

        app.setRule(rule);
        app.setFindSelector(new TermSelector("S.0"));
        app.setProofNode(p.getRoot());
        boolean b = app.getTermMatcher().leftMatch(pattern, term);
        assertTrue(b);
        assertEquals(Environment.getBoolType(), app.getTypeVariableMapping().get("b"));
        app.getTermMatcher().addInstantiation("%inst", makeTerm("3"));

        p.apply(app);

        ProofNode openGoal = p.getOpenGoals().get(0);

        assertEquals(2, openGoal.getNumber());

        Term result = openGoal.getSequent().getSuccedent().get(1);

        assertEquals(makeTerm("3>0"), result);
    }


    // from a bug
    public void testForallLeft() throws Exception {
        Binding term = (Binding) makeTerm("(\\forall i; i>0)");

        Rule rule = env.getRule("forall_left");
        Term pattern = rule.getFindClause().getTerm();
        Proof p = new Proof(new Sequent(Arrays.<Term>asList(term), Arrays.<Term>asList()), env);

        RuleApplicationMaker app = new RuleApplicationMaker(env);

        app.setRule(rule);
        app.setFindSelector(new TermSelector("A.0"));
        app.setProofNode(p.getRoot());
        app.getTermMatcher().leftMatch(pattern, term);
        app.getTermMatcher().addInstantiation("%inst", makeTerm("3"));

        p.apply(app);

        ProofNode openGoal = p.getOpenGoals().get(0);

        assertEquals(2, openGoal.getNumber());

        Term result = openGoal.getSequent().getAntecedent().get(1);

        assertEquals(makeTerm("3>0"), result);
    }

    public void testForallRight() throws Exception {
        Binding term = (Binding) makeTerm("(\\forall i; i>0)");

        Rule rule = env.getRule("forall_right");
        Proof p = new Proof(term, env);

        RuleApplicationMaker app = new RuleApplicationMaker(env);

        app.setRule(rule);
        app.setFindSelector(new TermSelector("S.0"));
        app.setProofNode(p.getRoot());
        app.matchInstantiations();

        p.apply(app);

        ProofNode openGoal = p.getOpenGoals().get(0);
        SymbolTable lst = openGoal.getLocalSymbolTable();
        assertNotNull(lst.getFunction("i"));

        Term result = openGoal.getSequent().getSuccedent().get(0);

        assertEquals(makeTerm("i > 0", lst), result);
    }


    public void testPrune() throws Exception {
        Term term = makeTerm("$and(b1, b2)");
        Proof p = new Proof(term, env);
        ProofNode orgRoot = p.getRoot();
        Rule rule = env.getRule("and_right");
        Term pattern = rule.getFindClause().getTerm();

        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0"));
        app.getTermMatcher().leftMatch(pattern, term);

        p.apply(app);
        p.prune(orgRoot);

        assertEquals(null, orgRoot.getChildren());
    }

    public void testPruneClosed() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term, env);
        ProofNode orgRoot = p.getRoot();
        Rule rule = env.getRule("close_true_right");

        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0"));

        p.apply(app);
        assertEquals(Collections.emptyList(), orgRoot.getChildren());

        p.prune(orgRoot);
        assertEquals(null, orgRoot.getChildren());
    }

    public void testPruneWrongProof() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term, env);
        Proof p2 = new Proof(term, env);

        try {
            p.prune(p2.getRoot());
            fail("Should have failed: Wrong proof for node");
        } catch (ProofException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testPrunedNode() throws Exception {
        Term term = makeTerm("true | true");
        Proof p = new Proof(term, env);
        Rule rule = env.getRule("or_right");

        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.getTermMatcher().leftMatch(rule.getFindClause().getTerm(), term);
        app.setFindSelector(new TermSelector("S.0"));
        p.apply(app);

        ProofNode node = p.getOpenGoals().get(0);

        p.prune(p.getRoot());
        app = new RuleApplicationMaker(env);
        app.setRule(env.getRule("close_true_right"));
        app.setProofNode(node);
        app.setFindSelector(new TermSelector("S.0"));

        try {
            p.apply(app);
            fail("Should fail because of a pruned proof node");
        } catch (ProofException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

    }

    public void testApplyTwice() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term, env);
        Rule rule = env.getRule("close_true_right");
        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0"));
        p.apply(app);

        try {
            p.apply(app);
            fail("should have failed: applied twice to a proof node");
        } catch (ProofException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testUncertifiedRuleApp() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term, env);
        Rule rule = env.getRule("and_right");
        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0"));

        RuleApplicationCertificate rac = new RuleApplicationCertificate(app, env);
        assertFalse(rac.verify());

        try {
            p.apply(rac);
            fail("should have failed: applied twice to a proof node");
        } catch (ProofException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

    }

    public void testRemovingRule() throws Exception {
        Term term = makeTerm("true");
        Proof p = new Proof(term, env);
        Rule rule = env.getRule("remove_right");
        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0"));
        app.getTermMatcher().addInstantiation("%a", makeTerm("true"));

        p.apply(app);

        Sequent s = p.getOpenGoals().get(0).getSequent();

        assertEquals(0, s.getAntecedent().size());
        assertEquals(0, s.getSuccedent().size());
    }

    // was a bug
    public void testReplaceSameTerm() throws Exception {
        env = makeEnv("include \"$base.p\"\n" +
        		"rule sillyReplace find 1 replace 1");
        Proof p = new Proof(makeTerm("1 = 1"), env);
        Rule rule = env.getRule("sillyReplace");
        RuleApplicationMaker app = new RuleApplicationMaker(env);
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0.0"));

        p.apply(app);
    }

    // was a bug
    public void testOptionalUpdate() throws Exception {
        env = makeEnv("include \"$base.p\"\n" +
                "rule sillyReplace find {U}1 replace 1");

        Proof p = new Proof(makeTerm("1 = 1"), env);
        Rule rule = env.getRule("sillyReplace");
        MutableRuleApplication app = new MutableRuleApplication();
        app.setRule(rule);
        app.setProofNode(p.getRoot());
        app.setFindSelector(new TermSelector("S.0.0"));
        app.getSchemaUpdateMapping().put("U", Update.EMPTY_UPDATE);

        try {
            p.apply(app);
            fail("should have failed: U was not an optional update");
        } catch (ProofException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }
}
