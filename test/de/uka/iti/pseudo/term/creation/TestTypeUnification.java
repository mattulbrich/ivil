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

    private Type setB;
    private Type setA;
    private TypeVariable tyvA = new TypeVariable("a");
    private TypeVariable tyvB = new TypeVariable("b");
    private TypeVariable tyvD = new TypeVariable("d");
    
    {
        try {
            setB = makeTerm("arb as set('b)").getType();
            setA = makeTerm("arb as set('a)").getType();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
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
    public void testRecoveryAfterException() throws Exception {
        
        TypeUnification tu = new TypeUnification();
        tu.leftUnify(tyvA, tyvB);
        try {
            tu.leftUnify(tyvA, setB);
            fail("Should have cyclic occur failure here");
        } catch(UnificationException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
        
        assertEquals(tu.instantiate(tyvA), tu.instantiate(tyvB));
    }

    public void testOccurCheck() throws Exception {

        // immediate
        TypeUnification tu = new TypeUnification();
        try {
            tu.leftUnify(tyvA, setA);
            fail("Should have cyclic occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }

        // mediate
        tu.leftUnify(tyvA, tyvB);
        try {
            tu.leftUnify(tyvA, setB);
            fail("Should have cyclic occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }
        
        tu = new TypeUnification();
        tu.leftUnify(tyvB, tyvA);
        // indirect
        try {
            tu.leftUnify(tyvA, setB);
            fail("Should have cyclic occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }
        
        // with unify
        try {
            tu.unify(tyvA, setB);
            fail("Should have cyclic occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }
        
        // on the right
        try {
            tu.unify(setB, tyvA);
            fail("Should have cyclic occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }
    }

    public void testTwice() throws Exception {
        
        TypeUnification tu = new TypeUnification();
        tu.leftUnify(tyvB, Environment.getBoolType());
        try {
            tu.leftUnify(tyvB, Environment.getIntType());
        } catch (UnificationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
    }
    
    // was a bug!
    public void testSuccessiveLeftInstantiation() throws Exception {
     
        Type ty = makeTerm("arb as poly('a, 'c)").getType();

        
        TypeUnification tu = new TypeUnification();
        
        tu.leftUnify(tyvA, tyvB);
        tu.leftUnify(tyvD, ty);
        
        assertEquals(tu.instantiate(ty), tu.instantiate(tyvD));
    }
    
    public void testSuccessiveInstantiation() throws Exception {
     
        Type ty = makeTerm("arb as poly('a, 'c)").getType();
        
        TypeUnification tu = new TypeUnification();
        
        tu.unify(tyvA, tyvB);
        tu.unify(ty, tyvD);
        
        assertEquals(tu.instantiate(ty), tu.instantiate(tyvD));
    }
}
