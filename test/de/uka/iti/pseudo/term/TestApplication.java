/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;

public class TestApplication extends TestCaseWithEnv {

    // due to a bug
    public void testCreateApplicationWithTypes() throws Exception {
        Application arb = new Application(env.getFunction("arb"), new TypeVariable("a"));
        Application two = new Application(env.getNumberLiteral("2"), Environment.getIntType());
        
        try {
            new Application(env.getFunction("$eq"), Environment.getBoolType(), new Term[] { arb, two });
            fail("should fail");
        } catch (TermException e) {
            // should fail
        }
    }
    
    public void testCreateApplicationWithTypes2() throws Exception {
        Application arb = new Application(env.getFunction("arb"), Environment.getBoolType());
        Application two = new Application(env.getNumberLiteral("2"), Environment.getIntType());
        
        try {
            new Application(env.getFunction("$eq"), Environment.getBoolType(), new Term[] { arb, two });
            fail("should fail");
        } catch (TermException e) {
            // should fail
        }
        
    }
    
    public void testCreateApplicationWithTypes3() throws Exception {
        Application arb = new Application(env.getFunction("arb"), Environment.getIntType());
        Application two = new Application(env.getNumberLiteral("2"), Environment.getIntType());
        
            Application eq = new Application(env.getFunction("$eq"), Environment.getBoolType(), new Term[] { arb, two });
            assertEquals(makeTerm("arb = 2"), eq);
    }
}
