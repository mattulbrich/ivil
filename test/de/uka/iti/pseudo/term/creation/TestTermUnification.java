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
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

public class TestTermUnification extends TestCaseWithEnv {
    
    private Term mt(String s) throws Exception {
        return makeTerm(s);
    }
    
    public void testLeftUnify1() throws Exception {
        TermUnification mc = new TermUnification(env);
        
        Term t1 = mt("%a");
        Term t2 = mt("2+2");
        boolean res = mc.leftUnify(t1, t2);
        assertTrue(res);
        assertEquals(t2, mc.instantiate(t1));
        
        mc = new TermUnification(env);
        res = mc.leftUnify(mt("%a + %i"), mt("2 + 3"));
        assertTrue(res);
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getIntType())));
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getBoolType())));
        assertEquals(mt("3"), mc.instantiate(mt("%i")));
        
        // the types have been set automatically!
        assertEquals(Environment.getIntType(), mc.getTypeUnification().instantiateSchemaType(new SchemaType("a")));
    }
    
    public void testUnifyIncomparable() throws Exception {
        TermUnification mc = new TermUnification(env);
        Term t1 = mt("g(%b, %a as int)");
        Term t2 = mt("g(0, true)");
        assertFalse(mc.leftUnify(t1, t2));
        assertNull(mc.getTermInstantiation().get("%a"));
        assertNull(mc.getTermInstantiation().get("%b"));
    }
    
    // from an early bug
    public void testInstantiateConst() throws Exception {
        TermUnification mc = new TermUnification(env);
        assertEquals(mt("true"), mc.instantiate(mt("true")));
    }
    
    public void testDiffInst() throws Exception {
        TermUnification mc = new TermUnification(env);
        
        assertFalse(mc.leftUnify(mt("%a + %a"), mt("2+3")));
    }
    
    public void testConsecutiveUnification() throws Exception {
        TermUnification mc = new TermUnification(env);
        
        assertTrue(mc.leftUnify(mt("%i"), mt("2")));
        assertTrue(mc.leftUnify(mt("%i1 + %i"), mt("4 + 2")));
        assertFalse(mc.leftUnify(mt("%i2 + %i"), mt("5 + 3")));
        // %i2 must not have been bound yet.
        assertTrue(mc.leftUnify(mt("%i2"), mt("7")));
    }

    public void testUpdates() throws Exception {
        TermUnification mc = new TermUnification(env);
        
        assertFalse(mc.leftUnify(mt("{ i1 := 0 }true"), mt("{ i1 := 1 }true")));
        assertTrue(mc.leftUnify(mt("{ i1 := 0 }true"), mt("{ i1 := 0 }true")));
    }
    
    /*
     * the program under test:
     *  0: assume b1
     *  1: assert b2
     *  2: skip
     *  3: goto 5, 0
     *  4: (* havov i1 *)
     *  5: i1 := i2 + i3
     *  6: end true
     *  7: end true
     *  8: skip_loopinv i1>0, i2
     */  
    public void testModalities() throws Exception {
        TermUnification mc = new TermUnification(env);
        Type bool = Environment.getBoolType();
        Type intTy = Environment.getIntType();
        Term.SHOW_TYPES = true;
        
        assertFalse(mc.leftUnify(mt("[%a : assert %b]"), mt("[0;P]")));
        assertTrue(mc.leftUnify(mt("[%a : assert %b]"), mt("[1;P]")));
        assertFalse(mc.leftUnify(mt("[%a]"), mt("[2;P]")));
        
        assertEquals(mt("b2"), mc.getTermFor(new SchemaVariable("%b", bool)));

        assertTrue(mc.leftUnify(mt("[%c : %x := %v]"), mt("[5;P]")));
        assertEquals(mt("i1"), mc.instantiate(new SchemaVariable("%x", intTy)));
        assertEquals(mt("i2+i3"), mc.instantiate(new SchemaVariable("%v", intTy)));
        
        assertFalse(mc.leftUnify(mt("[0;P]"), mt("[0;Q]")));
        
        // was a bug
        assertFalse(mc.leftUnify(mt("[[%d]]"), mt("[7;P]")));
        // was a bug
        assertFalse(mc.leftUnify(mt("[[7;P]]"), mt("[7;P]")));
        
        assertTrue(mc.leftUnify(mt("[%e]"), mt("[6;P]")));
        // cannot match because not same number even though same statement
        assertFalse(mc.leftUnify(mt("[%e]"), mt("[7;P]")));
        
        // beyond program range
        assertTrue(mc.leftUnify(mt("[%f : end true]"), mt("[100;P]")));
        
        // skip matching
        assertFalse(mc.leftUnify(mt("[%g : skip]"), mt("[8;P]")));
        assertTrue(mc.leftUnify(mt("[%g : skip]"), mt("[2;P]")));
        
        assertFalse(mc.leftUnify(mt("[%h : skip_loopinv %inv]"), mt("[8;P]")));
        assertTrue(mc.leftUnify(mt("[%h : skip_loopinv %inv, %var]"), mt("[8;P]")));;
        assertEquals(mt("i1 > 0"), mc.instantiate(new SchemaVariable("%inv", bool)));
        assertEquals(mt("i2"), mc.instantiate(new SchemaVariable("%var", intTy)));
    }
    
    
    public void testTyping() throws Exception {
        TermUnification mc = new TermUnification(env);
        mc.leftUnify(mt("%a"), mt("2"));
        
        assertEquals(mt("arb = 2"), mc.instantiate(mt("arb = %a as int")));
    }
    
    public void testDoubling() throws Exception {
        
        TermUnification mc = new TermUnification(env);
        assertFalse(mc.leftUnify(mt("g(arb as %'a, arb as %'a)"), mt("g(arb as int,arb as bool)")));
        mc = new TermUnification(env);
        assertFalse(mc.leftUnify(mt("(\\T_all %'b; true)"), mt("(\\T_all 'a; true as %'b)")));
        mc = new TermUnification(env);
        assertFalse(mc.leftUnify(mt("(\\T_all %'a; true) & arb as %'a = arb"), mt("(\\T_all %'a; true) & arb as int = arb")));
        
    }
    
    public void testOccurCheck() throws Exception {
        TermUnification mc = new TermUnification(env);
        try {
            mc.addInstantiation(new SchemaVariable("%a", Environment
                    .getIntType()), mt("%a + 2"));
            fail("should fail");
        } catch (TermException e) {
            // should fail
        }
    }
    
    // was a bug
    public void testSchemaFind() throws Exception {
        assertTrue(TermUnification.containsSchemaVariables(mt("{ %c := 0 }true")));
        assertTrue(TermUnification.containsSchemaVariables(mt("(\\forall %c; true)")));
        assertTrue(TermUnification.containsSchemaVariables(mt("[%a]")));
        // from another one:
        assertTrue(TermUnification.containsSchemaVariables(makeTerm("(\\forall i; %a > i)")));
    }
    
    // TODO Test binders!
    
    public void testSchemaUpdates() throws Exception {
        TermUnification mc = new TermUnification(env);
        mc.leftUnify(mt("{U}%a"), mt("{i1:=0}b2"));
        
        assertEquals(mt("{i1:=0}i1"), mc.instantiate(mt("{U}i1")));
        assertEquals(mt("{V}b2"), mc.instantiate(mt("{V}%a")));
    }
    
    public void testTypeQuantification() throws Exception {
    
        TermUnification mc = new TermUnification(env);
        
        mc.leftUnify(mt("(\\T_all %'a; arb = arb as %'a)"), mt("(\\T_all 'a; arb = arb as 'a)"));
        
        assertFalse(mc.leftUnify(mt("(\\T_all %'b; true)"), mt("(\\T_all 'a; true as %'b)")));
        
        assertEquals(new TypeVariable("a"),
                mc.getTypeUnification().instantiateSchemaType(new SchemaType("a")));
    }
    
}
