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

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMatcher;

public class TestWhereConditions extends TestCaseWithEnv {

    public void testProgramFree() throws Exception {
        
        ProgramFree pf = new ProgramFree();
        assertTrue(pf.check(null, new Term[] { makeTerm("1+2 = 3") }, null, env));
        assertFalse(pf.check(null, new Term[] { makeTerm("true & [5;P]") }, null, env));
        
    }
    
    public void testIntLiteral() throws Exception {
        IntLiteral intLit = new IntLiteral();
        
        assertTrue(intLit.check(null, new Term[] { makeTerm("22") }, null, env));
        assertFalse(intLit.check(null, new Term[] { makeTerm("2+2") }, null, env));
    }

    public void testDistinctAssumeAndFind() throws Exception {
        DistinctAssumeAndFind daf = new DistinctAssumeAndFind();
        
        MutableRuleApplication ra = new MutableRuleApplication();
        ra.setFindSelector(new TermSelector("A.1.1"));
        ra.getAssumeSelectors().add(new TermSelector("A.0"));
        assertTrue(daf.check(null, null, ra, DEFAULT_ENV));
    }
    
    public void testCanEval() throws Exception {
        CanEvaluateMeta can = new CanEvaluateMeta();
        
        assertTrue(can.check(null, new Term[] { makeTerm("$$intEval(1+1)") }, null, env));
        assertFalse(can.check(null, new Term[] { makeTerm("$$intEval(i1+1)") }, null, env));
        assertFalse(can.check(null, new Term[] { makeTerm("$$skolem(1)") }, new RuleApplicationMaker(env), env));
        
    }
    
    public void testFreshVar() throws Exception {
        FreshVariable fresh = new FreshVariable();
        
        assertTrue(fresh.check(null, new Term[] { makeTerm("\\var x as int"), makeTerm("(\\forall x as int; true)")}, null, env));
        assertFalse(fresh.check(null, new Term[] { makeTerm("\\var x as int"), makeTerm("\\var x + 2")}, null, env));
        
        TermMatcher tm = new TermMatcher();
        fresh.addInstantiations(tm, new Term[] { makeTerm("%x as int"), makeTerm("\\var x > 0"), makeTerm("(\\forall x1; x1 > 0)") });
        Term result = tm.getTermInstantiation().get("%x");
        assertEquals(makeTerm("\\var x2 as int"), result);
        assertTrue(fresh.check(null, new Term[] { result, makeTerm("\\var x > 0"), makeTerm("(\\forall x1; x1 > 0)") }, null, env));
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
    
    public void testNoFree() throws Exception {
        
        env = makeEnv("include \"$int.p\" " +
        		"function int i1 " +
        		"program P assert \\var b");
        
        NoFreeVars noFree = new NoFreeVars();
        
        assertFalse(checkNoFree(noFree, "\\var x as int"));
        assertTrue(checkNoFree(noFree, "i1 as int"));
        assertTrue(checkNoFree(noFree, "(\\forall x; \\var x > 0)"));
        assertFalse(checkNoFree(noFree, "(\\forall x; \\var x > 0) & \\var x < 0"));
        assertFalse(checkNoFree(noFree, "[0;P]"));
        assertTrue(checkNoFree(noFree, "(\\forall b as bool; [0;P])"));
    }

    private boolean checkNoFree(NoFreeVars noFree, String s) throws RuleException,
            Exception {
        return noFree.check(null, new Term[] { makeTerm(s) }, null, env);
    }

}
