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
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;

public class TestWhereConditions extends TestCaseWithEnv {

    public void testProgramFree() throws Exception {
        
        ProgramFree pf = new ProgramFree();
        assertTrue(pf.check(null, new Term[] { makeTerm("1+2 = 3") }, null, null, env));
        assertFalse(pf.check(null, new Term[] { makeTerm("true & [5;P]") }, null, null, env));
        
    }
    
    public void testIntLiteral() throws Exception {
        IntLiteral intLit = new IntLiteral();
        
        assertTrue(intLit.check(null, new Term[] { makeTerm("22") }, null, null, env));
        assertFalse(intLit.check(null, new Term[] { makeTerm("2+2") }, null, null, env));
    }

    public void testDistinctAssumeAndFind() throws Exception {
        DistinctAssumeAndFind daf = new DistinctAssumeAndFind();
        
        MutableRuleApplication ra = new MutableRuleApplication();
        ra.setFindSelector(new TermSelector("A.1.1"));
        ra.getAssumeSelectors().add(new TermSelector("A.0"));
        assertTrue(daf.check(null, null, ra, null, DEFAULT_ENV));
    }
    
    public void testCanEval() throws Exception {
        CanEvaluateMeta can = new CanEvaluateMeta();
        
        assertTrue(can.check(null, new Term[] { makeTerm("$$intEval(1+1)") }, null, null, env));
        assertFalse(can.check(null, new Term[] { makeTerm("$$intEval(i1+1)") }, null, null, env));
        assertFalse(can.check(null, new Term[] { makeTerm("$$skolem(1)") }, new RuleApplicationMaker(env), null, env));
        
    }
    
    public void testInteractive() throws Exception {
        Interactive inter = new Interactive();
        Term intX = makeTerm("%x as int");
        Term alphaX = makeTerm("%x as %'x");
        
        inter.checkSyntax(new Term[] { intX });
        inter.checkSyntax(new Term[] { intX, Environment.getFalse() });
        inter.checkSyntax(new Term[] { alphaX, Environment.getTrue() });
        inter.checkSyntax(new Term[] { intX, Environment.getFalse() });
        
        try {
            inter.checkSyntax(new Term[] { intX, Environment.getTrue() });
            fail("should complain that x has not a type var type");
        } catch(RuleException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
        
        try {
            inter.checkSyntax(new Term[] { intX, intX });
            fail("should complain that 2nd arg has wrong kind");
        } catch(RuleException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
        
        try {
            inter.checkSyntax(new Term[] { });
            fail("should complain that no args");
        } catch(RuleException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
        
        try {
            inter.checkSyntax(new Term[] { Environment.getFalse()});
            fail("should complain that 1st argument not schema");
        } catch(RuleException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }

    }
    
}
