/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.io.StringReader;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class TestFileParser extends TestCase {

    private Environment testEnv(String string) throws Exception {
        FileParser fp = new FileParser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "test");
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
            System.out.println(e.getMessage());
        }
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
    
    public void testRules() throws Exception {
        // no replace in newgoals (was a bug) 
        assertEnvFail("rule something find {%a} newgoal replace {%b}");
        assertEnvFail("rule something find {1} assume {%b} |- samegoal replace {%b+1}");
    }
    
    public void testFindReplaceSameType() throws Exception {
        assertEnvFail("rule something find {1} newgoal replace {true}");
        
        Environment e = testEnv("rule something find {1} samegoal replace {%a}");
    }
    
    // due to problems with cuts
    public void testGoalActionNaming() throws Exception {
        Environment e = testEnv("rule something find {1} samegoal \"actionname\" replace {2}");
        assertEquals("actionname", e.getRule("something").getGoalActions()[0].getName());
        
    }

    
}