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
package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;

public class TestTypeUnification extends TestCaseWithEnv {

    // due to a bug
    public void testBoolInt() throws Exception {
        TypeUnification tu = new TypeUnification();
        
        try {
            tu.leftUnify(Environment.getBoolType(), Environment.getIntType());
            fail("Should fail");
        } catch (UnificationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    // error in type unification
    public void testCycle() throws Exception {
        
        Type setB = makeTerm("arb as set('b)").getType();
        Type setA = makeTerm("arb as set('a)").getType();
        
        TypeUnification tu = new TypeUnification();
        tu.leftUnify(new TypeVariable("b"), new TypeVariable("a"));
        tu.leftUnify(new TypeVariable("a"), setB);
        
        // was: 'a --> set('a)
        assertEquals(setB, tu.instantiate(new TypeVariable("a")));
        
        tu.leftUnify(new TypeVariable("c"), Environment.getBoolType());
        
        // failed: 'a --> set(set('a))
        assertEquals(setB, tu.instantiate(new TypeVariable("a")));
        
    }
    
    public void testTwice() throws Exception {
        
        Type setB = makeTerm("arb as set('b)").getType();
        Type setA = makeTerm("arb as set('a)").getType();
        
        TypeUnification tu = new TypeUnification();
        tu.leftUnify(new TypeVariable("b"), Environment.getBoolType());
        try {
            tu.leftUnify(new TypeVariable("b"), Environment.getIntType());
        } catch (UnificationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
    }
}
