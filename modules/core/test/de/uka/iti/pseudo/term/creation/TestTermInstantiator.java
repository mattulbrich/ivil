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

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;

public class TestTermInstantiator extends TestCaseWithEnv {
    
    Map<String, Term> termmap;
    Map<String, Type> typemap;
    Map<String, Update> updatemap;
    TermInstantiator inst;
    
    protected void setUp() throws Exception {
        termmap = new HashMap<String, Term>();
        typemap = new HashMap<String, Type>();
        updatemap = new HashMap<String, Update>();
        inst = new TermInstantiator(termmap, typemap, updatemap);
    }

    // from a bug
    public void testInstantiateTerm() throws Exception {
        
        Term orig = makeTerm("(\\exists %x; %x = 5)");
        termmap.put("%x", Variable.getInst("xx", Environment.getIntType()));
        assertEquals(makeTerm("(\\exists xx; xx = 5)"), inst.instantiate(orig));
    }
    
    // from a bug
    public void testInstantiateUnusedBoundSchemaVar() throws Exception {
        Term orig = makeTerm("(\\exists %x as int; true)");
        termmap.put("%x", Variable.getInst("xx", Environment.getIntType()));
        assertEquals(makeTerm("(\\exists xx as int; true)"), inst.instantiate(orig));
    }
    
    public void testInUpdates() throws Exception {
        
        Term orig = makeTerm("{ %c := %d+1}true");
        termmap.put("%c", makeTerm("i1"));
        termmap.put("%d", makeTerm("22"));
        // System.out.println("{ i1 := $plus(22,1) }true");
        // System.out.println(inst.instantiate(orig));
        assertEquals(makeTerm("{ i1 := $plus(22,1) }true"), inst.instantiate(orig));
    }

    // i2 is not assignable
    public void testInUpdates2() throws Exception {
        
        Term orig = makeTerm("{ %c := 0 }true");
        termmap.put("%c", makeTerm("i2"));
        
        try {
            inst.instantiate(orig);
            fail("i2 is not assignable - should have failed");
        } catch (Exception e) {
        }
    }
    
    // from a bug:
    public void testInUpdates3() throws Exception {
        termmap.put("%c", makeTerm("i1"));

        Term orig = makeTerm("{ %c := 0 }true");
        assertEquals(makeTerm("{ i1 := 0 }true"), inst.instantiate(orig));
        
        orig = makeTerm("{ i1 := %c }true");
        assertEquals(makeTerm("{ i1 := i1 }true"), inst.instantiate(orig));
    }
    
    
    // partially from a bug
    public void testUpdate() throws Exception {
        
        termmap.put("%x", makeTerm("i1"));
        termmap.put("%v", makeTerm("2"));
        typemap.put("v", Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("{%x:=%v}i2"));
        assertEquals(makeTerm("{ i1 := 2 }i2"), t);
        
        t = inst.instantiate(makeTerm("{%x:=%v}%x"));
        assertEquals(makeTerm("{ i1 := 2 }i1"), t);
    }

    public void testSchemaProgram() throws Exception {
        
        termmap.put("%a", makeTerm("[2; P]true"));
        
        Term t = inst.instantiate(makeTerm("%a as bool"));
        assertEquals(makeTerm("[2; P]true"), t);
        
        t = inst.instantiate(makeTerm("[%a]false"));
        assertEquals(makeTerm("[2; P]true"), t);
        
        try {
            t = inst.instantiate(makeTerm("[[%a; P]]true"));
            fail("wrong termination - should have failed");
        } catch (Exception e) {
        }
    }
    
    // due to problems
    // [1] is assert b2 
    public void testProgramComparingInstantiation() throws Exception {
        
        inst = new ProgramComparingTermInstantiator(termmap, typemap, updatemap, env);
        termmap.put("%a", makeTerm("[1;P]true"));
        termmap.put("%b", makeTerm("b2"));
        
        // it suffices if it does not fail
        inst.instantiate(makeTerm("[%a : assert b2]true"));
        inst.instantiate(makeTerm("[%a : assert %b]true"));
        inst.instantiate(makeTerm("[%a]true"));
        
        try {
            inst.instantiate(makeTerm("[%a : assume b2]true"));
            fail("should have failed");
        } catch (Exception e) {
        }
        
        try {
            inst.instantiate(makeTerm("[%a : assert b1]true"));
            fail("should have failed");
        } catch (Exception e) {
        }
        
        try {
            inst.instantiate(makeTerm("[%a : assert %c]true"));
            fail("should have failed");
        } catch (Exception e) {
        }
    }
    
    // from a bug
    // the type of the bound variable is not updated
    public void testBindingInstantiation() throws Exception {
        Term.SHOW_TYPES = true;
        termmap.put("%v", makeTerm("1"));
        typemap.put("v", Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("(\\forall x; x = %v)"));
        assertEquals(makeTerm("(\\forall x; x = 1)"), t);
    }
    
    public void testTypeQuant() throws Exception {
        typemap.put("v", TypeVariable.getInst("inst"));
        typemap.put("w", Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("(\\T_all %'v; true)"));
        assertEquals(makeTerm("(\\T_all 'inst; true)"), t);
        
        try {
            inst.instantiate(makeTerm("(\\T_all %'w; true)"));
            fail("should have failed. int has been bound");
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public void testSchemaUpdates() throws Exception {
        UpdateTerm ut = (UpdateTerm) makeTerm("{ i1 := 0 || b1 := false }true");
        updatemap.put("U", ut.getUpdate());
        termmap.put("%a", makeTerm("33"));
        typemap.put("a", Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("false & { U }false"));
        assertEquals(makeTerm("false & { i1 := 0 || b1 := false } false"), t);
        
        t = inst.instantiate(makeTerm("{ U }%a"));
        assertEquals(makeTerm("{ i1 := 0 || b1 := false }33"), t);
    }
    
    // was a bug
    public void testComposedTypes() throws Exception {
        Term.SHOW_TYPES = true;
        typemap.put("a", Environment.getIntType());
        
        Term t0 = makeTerm("arb as poly(%'a, 'b)");
        Term t = inst.instantiate(t0);
        assertEquals(makeTerm("arb as poly(int, 'b)"), t);

    }
}
