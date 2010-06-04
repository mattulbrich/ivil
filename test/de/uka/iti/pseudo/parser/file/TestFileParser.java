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
package de.uka.iti.pseudo.parser.file;

import java.io.StringReader;

import junit.framework.TestCase;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Parser;

public class TestFileParser extends TestCase {

    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        env.dump();
        return env;
    }
    
    private void assertEnvFail(String string) throws Exception {
        try {
            testEnv(string);
            fail("should fail");
        } catch(ASTVisitException e) {
            // this should happen
            // System.out.println(e.getMessage());
        }
    }
    
    public void testIntLoadable() throws Exception {
        testEnv("include \"$int.p\"");
    }

    public void testPolymorphSorts() throws Exception {
        Environment env = testEnv("sort poly('a, 'b)");
        assertEquals("Sort[poly;2]", env.getSort("poly").toString());
        
        env = testEnv("sort poly('a, 'b)");
        assertEquals("Sort[poly;2]", env.getSort("poly").toString());
    }
    
    public void testAssignableFunctions() throws Exception {
        // assignables must be nullary
        assertEnvFail("function int f(int) assignable");
        // assignables must not have type vars in type
        assertEnvFail("function 'a f assignable");
        assertEnvFail("sort S('a) function S('a) f assignable");
    }
    
    // from syntax errors
    public void testRules2() throws Exception {
        testEnv("function int f(int) rule r find f(0)=0 |- samegoal replace 1=f(1) as int");
        testEnv("problem arb=1");
    }
    
    public void testRules() throws Exception {
        // no replace in newgoals (was a bug) 
        assertEnvFail("rule something find %a newgoal replace %b");
        assertEnvFail("rule something find 1 assume %b |- samegoal replace %b+1");
    }
    
    // test findless rules
    public void testFindless() throws Exception {
        testEnv("rule cut_findless samegoal add |- %b samegoal add %b |-");
        assertEnvFail("rule may_not_replace samegoal replace %b");
        assertEnvFail("rule may_not_remove samegoal remove");
    }
    
    public void testFindReplaceSameType() throws Exception {
        assertEnvFail("rule something find 1 newgoal replace true");
        
        Environment e = testEnv("rule something find 1 samegoal replace %a");
    }
    
    // due to problems with cuts
    public void testGoalActionNaming() throws Exception {
        Environment e = testEnv("rule something find 1 samegoal \"actionname\" replace 2");
        assertEquals("actionname", e.getRule("something").getGoalActions().get(0).getName());
    }
    
    public void testProtectedProduction() throws Exception {
        testEnv("function `int function(int)` rule `find` find `function(0)` samegoal replace `function(1) as int`");
        testEnv("problem `1` `` ` =1`");
    }

    public void testPrograms() throws Exception {
        Environment e = testEnv("program P assert true program Q assume true");
        if(TestCaseWithEnv.VERBOSE)
            e.dump();
        assertNotNull(e.getProgram("P"));
        assertNotNull(e.getProgram("Q"));
        assertNull(e.getProgram("Unknown"));
    }
    
    public void testProgramTextAnnotation() throws Exception {
        Environment e = testEnv("program P skip skip; \"hello world\" skip");
        if(TestCaseWithEnv.VERBOSE)
            e.dump();
        Program P = e.getProgram("P");
        assertNull(P.getTextAnnotation(0));
        assertEquals("hello world", P.getTextAnnotation(1));
        assertNull(P.getTextAnnotation(2));
        assertNull(P.getTextAnnotation(3));
        
    }
    
}
