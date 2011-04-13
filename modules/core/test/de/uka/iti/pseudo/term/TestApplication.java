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

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTLocatedElement;

public class TestApplication extends TestCaseWithEnv {

    // no place to put it but here
    public void testSchemaInFunctionDef() throws Exception {
        try {
            Function f = new Function("fail", new SchemaType("sch"),
                    new Type[0],
                    false, false, ASTLocatedElement.CREATED);
            System.out.println(f);
            fail("Should fail due to schema types");
        } catch (Exception e) {
            if (VERBOSE)
                e.printStackTrace();
        }
        try {
            Function f = new Function("fail", Environment.getBoolType(),
                    new Type[] { new SchemaType("sch2") }, false, false,
                    ASTLocatedElement.CREATED);
            System.out.println(f);
            fail("Should fail due to schema types");
        } catch (Exception e) {
            if (VERBOSE)
                e.printStackTrace();
        }
    }
    
    // due to a bug
    public void testCreateApplicationWithTypes() throws Exception {
        Application arb = Application.create(env.getFunction("arb"), new TypeVariable("a"));
        Application two = Application.create(env.getNumberLiteral("2"), Environment.getIntType());
        
        try {
            Application.getInst(env.getFunction("$eq"), Environment.getBoolType(), new Term[] { arb, two });
            fail("should fail");
        } catch (TermException e) {
            // should fail
        }
    }
    
    public void testCreateApplicationWithTypes2() throws Exception {
        Application arb = Application.create(env.getFunction("arb"), Environment.getBoolType());
        Application two = Application.create(env.getNumberLiteral("2"), Environment.getIntType());
        
        try {
            Application.getInst(env.getFunction("$eq"), Environment.getBoolType(), new Term[] { arb, two });
            fail("should fail");
        } catch (TermException e) {
            // should fail
        }
        
    }
    
    public void testCreateApplicationWithTypes3() throws Exception {
        Application arb = Application.create(env.getFunction("arb"), Environment.getIntType());
        Application two = Application.create(env.getNumberLiteral("2"), Environment.getIntType());
        
            Application eq = Application.getInst(env.getFunction("$eq"), Environment.getBoolType(), new Term[] { arb, two });
            assertEquals(makeTerm("arb = 2"), eq);
    }
}
