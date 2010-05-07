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
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestUpdSimplification extends TestCaseWithEnv {

    MutableRuleApplication ra = new MutableRuleApplication();
    MetaEvaluator eval = new MetaEvaluator(ra, env);

    private void assertEvalsTo(String t1, String t2) throws Exception {
        Term t = makeTerm(t1);
        assertEquals(makeTerm(t2), eval.evalutate(t));
    }

    public void testUpdateSimplification() throws Exception {
        assertEvalsTo("$$updSimpl({i1 := 1}i1)", "1");
        assertEvalsTo("$$updSimpl({i1 := 1}i2)", "i2");

        assertEvalsTo("$$updSimpl({i1 := 1}g(i1, i2))",
                "g({i1 := 1}i1, {i1 := 1}i2)");

        assertEvalsTo("$$updSimpl({i1 := 1}(\\forall x;x > i1))",
                "(\\forall x;{i1 := 1}(x>i1))");

        // variables also ... but this can be taken for granted

        assertEvalsTo("$$updSimpl({i1 := 1}{b1:=i1>0}b1)",
                "{i1 :=1 || b1 := {i1:=1}(i1>0)}b1");

        try {
            eval.evalutate(makeTerm("$$updSimpl({i1:=1}[0;P])"));
            fail("updated program term should not evaluate");
        } catch (TermException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }

    }
    
    public void testDeepUpdateSimplification() throws Exception {
        assertEvalsTo("$$deepUpdSimpl({i1 := 1}i1)", "1");
        assertEvalsTo("$$deepUpdSimpl({i1 := 1}i2)", "i2");

        assertEvalsTo("$$deepUpdSimpl({i1 := 1}g(i1, i2))",
                "g(1, i2)");

        assertEvalsTo("$$deepUpdSimpl({i1 := 1}(\\forall x;x > i1))",
                "(\\forall x;x>1)");

        // variables also ... but this can be taken for granted

        assertEvalsTo("$$deepUpdSimpl({i1 := 1}{b1:=i1>0}b1)",
                "1>0");
        
        // also embedded
        assertEvalsTo("$$deepUpdSimpl(1+{i1:=1}i1)", "1+1");
        
        // fail if no update
        try {
            eval.evalutate(makeTerm("$$updSimpl(1)"));
            fail("no updated term should not evaluate");
        } catch (TermException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }

        
        // fail if updated program
        try {
            eval.evalutate(makeTerm("$$updSimpl({i1:=1}[0;P])"));
            fail("updated program term should not evaluate");
        } catch (TermException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }

    }


}
