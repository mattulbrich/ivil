/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.io.FileNotFoundException;
import java.io.StringReader;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class TestFileParser extends TestCase {

    private Environment testEnv(String string) throws Exception {
        FileParser fp = new FileParser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "test");
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
        assertEnvFail("function int f(int) assignable");
        assertEnvFail("function 'a f assignable");
    }

    
}