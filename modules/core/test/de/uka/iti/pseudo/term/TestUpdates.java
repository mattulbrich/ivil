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
package de.uka.iti.pseudo.term;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.statement.Assignment;

public class TestUpdates extends TestCaseWithEnv {

    public void testCreation() throws Exception {
        assertEquals(makeTerm("{i1 := 3}3"), makeTerm("{i1 := 3}3"));
    }
    
    public void testOptionalMatching() throws Exception {
        
        TermMatcher tm = new TermMatcher();
        
        assertTrue(tm.leftMatch(makeTerm("{ U ?}%a"), makeTerm("true")));
        assertEquals(makeTerm("true"), tm.instantiate(makeTerm("{ U ?}%a")));
        
        assertTrue(tm.leftMatch(makeTerm("{ V ?}%b"), makeTerm("{i1:=1}i1")));
        assertEquals(makeTerm("{i1:=1}i1"), tm.instantiate(makeTerm("{ V ?}%b")));
    }
    
    public void testRules() throws Exception {
        env = makeEnv("function int i1 assignable\n" +
        		"function int i2\n" +
        		"rule i1_i2 find {U?}i1 replace {U}i2\n" +
        		"tags rewrite \"r\" axiomName \"a2\" ");
        
        List<Rule> ruleList = Collections.singletonList(env.getRule("i1_i2"));
        {
            Proof proof = new Proof(makeTerm("{i1:=1}i1"));
            RuleApplicationFinder raf = new RuleApplicationFinder(proof, proof.getRoot(), env);
            List<RuleApplication> apps = raf.findAll(new TermSelector("S.0"), ruleList);
            assertEquals(1, apps.size());
            RuleApplication app = apps.get(0);
            assertEquals("S.0", app.getFindSelector().toString());
            assertEquals(new Update(new Assignment[] { new Assignment(makeTerm("i1"), makeTerm("1"))}), 
                    app.getSchemaUpdateMapping().get("U"));
            
            proof.apply(app, env);
            assertEquals(makeTerm("{i1:=1}i2"), 
                    proof.getOpenGoals().get(0).getSequent().getSuccedent().get(0));
        }
        {
            Proof proof = new Proof(makeTerm("i1"));
            RuleApplicationFinder raf = new RuleApplicationFinder(proof, proof.getRoot(), env);
            List<RuleApplication> apps = raf.findAll(new TermSelector("S.0"), ruleList);
            assertEquals(1, apps.size());
            RuleApplication app = apps.get(0);
            assertEquals("S.0", app.getFindSelector().toString());
            assertEquals(Update.EMPTY_UPDATE, 
                    app.getSchemaUpdateMapping().get("U"));
            
            proof.apply(app, env);
            assertEquals(makeTerm("i2"), 
                    proof.getOpenGoals().get(0).getSequent().getSuccedent().get(0));
        }
    }
    
}
