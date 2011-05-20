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
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;

public class TestTypeUnification extends TestCaseWithEnv {

    private Type setB;
    private Type setA;
    private SchemaType tyvA = new SchemaType("a");
    private SchemaType tyvB = new SchemaType("b");
    private SchemaType tyvD = new SchemaType("d");
    
    {
        try {
            setB = makeTerm("arb as set(%'b)").getType();
            setA = makeTerm("arb as set(%'a)").getType();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    // due to a bug
    public void testBoolInt() throws Exception {
        TypeUnification tu = new TypeUnification();
        
        try {
            tu.unify(Environment.getBoolType(), Environment.getIntType());
            fail("Should fail");
        } catch (UnificationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    // error in type unification
    public void testRecoveryAfterException() throws Exception {
        
        TypeUnification tu = new TypeUnification();
        tu.unify(tyvA, tyvB);
        try {
            tu.unify(tyvA, setB);
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
            tu.unify(tyvA, setA);
            fail("Should have occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }

        // mediate
        tu.unify(tyvA, tyvB);
        try {
            tu.unify(tyvA, setB);
            fail("Should have cyclic occur failure here");
        } catch (UnificationException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }
        
        tu = new TypeUnification();
        tu.unify(tyvB, tyvA);
        // indirect
        try {
            tu.unify(tyvA, setB);
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
        tu.unify(tyvB, Environment.getBoolType());
        try {
            tu.unify(tyvB, Environment.getIntType());
        } catch (UnificationException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
    }
    
    // was a bug!
    public void testSuccessiveLeftInstantiation() throws Exception {
     
        Type ty = makeTerm("arb as poly('a, 'c)").getType();

        
        TypeUnification tu = new TypeUnification();
        
        tu.unify(tyvA, tyvB);
        tu.unify(tyvD, ty);
        
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
