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
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;

public class TestTermUnification extends TestCaseWithEnv {
    
    private Term mt(String s) throws Exception {
        return makeTerm(s);
    }
    
    public void testLeftUnify1() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        
        Term t1 = mt("%a");
        Term t2 = mt("2+2");
        boolean res = mc.leftMatch(t1, t2);
        assertTrue(res);
        assertEquals(t2, mc.instantiate(t1));
        
        mc = new TermMatcher(env);
        res = mc.leftMatch(mt("%a + %i"), mt("2 + 3"));
        assertTrue(res);
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getIntType())));
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getBoolType())));
        assertEquals(mt("3"), mc.instantiate(mt("%i")));
        
        // the types have been set automatically!
        assertEquals(Environment.getIntType(), mc.getTypeInstantiation().get("a"));
    }
    
    public void testUnifyIncomparable() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        Term t1 = mt("g(%b, %a as int)");
        Term t2 = mt("g(0, true)");
        assertFalse(mc.leftMatch(t1, t2));
        assertNull(mc.getTermInstantiation().get("%a"));
        assertNull(mc.getTermInstantiation().get("%b"));
    }
    
    // from an early bug
    public void testInstantiateConst() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        assertEquals(mt("true"), mc.instantiate(mt("true")));
    }
    
    public void testDiffInst() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        
        assertFalse(mc.leftMatch(mt("%a + %a"), mt("2+3")));
    }
    
    public void testConsecutiveUnification() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        
        assertTrue(mc.leftMatch(mt("%i"), mt("2")));
        assertTrue(mc.leftMatch(mt("%i1 + %i"), mt("4 + 2")));
        assertFalse(mc.leftMatch(mt("%i2 + %i"), mt("5 + 3")));
        // %i2 must not have been bound yet.
        assertTrue(mc.leftMatch(mt("%i2"), mt("7")));
    }

    public void testUpdates() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        
        assertFalse(mc.leftMatch(mt("{ i1 := 0 }true"), mt("{ i1 := 1 }true")));
        assertTrue(mc.leftMatch(mt("{ i1 := 0 }true"), mt("{ i1 := 0 }true")));
    }
    
    /*
     * the program under test:
     *  0: assume b1
     *  1: assert b2
     *  2: skip
     *  3: goto 5, 0
     *  4: havoc i1
     *  5: i1 := i2 + i3
     *  6: end true
     *  7: end true
     *  8: skip_loopinv i1>0, i2
     *  9: i1:=1 || b1 := true
     */  
    public void testModalities() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        Type bool = Environment.getBoolType();
        Type intTy = Environment.getIntType();
        Term.SHOW_TYPES = true;
        
        assertFalse(mc.leftMatch(mt("[%a : assert %b]"), mt("[0;P]")));
        assertTrue(mc.leftMatch(mt("[%a : assert %b]"), mt("[1;P]")));
        assertFalse(mc.leftMatch(mt("[%a]"), mt("[2;P]")));
        
        assertEquals(mt("b2"), mc.getTermFor(new SchemaVariable("%b", bool)));
        
        assertTrue(mc.leftMatch(mt("[%c : %x := %v]"), mt("[5;P]")));
        assertEquals(mt("i1"), mc.instantiate(new SchemaVariable("%x", intTy)));
        assertEquals(mt("i2+i3"), mc.instantiate(new SchemaVariable("%v", intTy)));
        
        assertFalse(mc.leftMatch(mt("[0;P]"), mt("[0;Q]")));
        
        // was a bug
        assertFalse(mc.leftMatch(mt("[[%d]]"), mt("[7;P]")));
        // was a bug
        assertFalse(mc.leftMatch(mt("[[7;P]]"), mt("[7;P]")));
        
        assertTrue(mc.leftMatch(mt("[%e]"), mt("[6;P]")));
        // cannot match because not same number even though same statement
        assertFalse(mc.leftMatch(mt("[%e]"), mt("[7;P]")));
        
        // beyond program range
        assertTrue(mc.leftMatch(mt("[%f : end true]"), mt("[100;P]")));
        
        // skip matching
        assertFalse(mc.leftMatch(mt("[%g : skip]"), mt("[8;P]")));
        assertTrue(mc.leftMatch(mt("[%g : skip]"), mt("[2;P]")));
        
        assertFalse(mc.leftMatch(mt("[%h : skip_loopinv %inv]"), mt("[8;P]")));
        assertTrue(mc.leftMatch(mt("[%h : skip_loopinv %inv, %var]"), mt("[8;P]")));;
        assertEquals(mt("i1 > 0"), mc.instantiate(new SchemaVariable("%inv", bool)));
        assertEquals(mt("i2"), mc.instantiate(new SchemaVariable("%var", intTy)));
        
        assertTrue(mc.leftMatch(mt("[%i : havoc %j]"), mt("[4;P]")));
        assertEquals(mt("i1"), mc.instantiate(new SchemaVariable("%j", intTy)));
        
        Update upd = ((UpdateTerm)mt("{i1:=1||b1:=true}true")).getUpdate();
        assertTrue(mc.leftMatch(mt("[%k : U]"), mt("[9;P]")));
        assertEquals(upd, mc.getUpdateFor("U"));
        
        assertTrue(mc.leftMatch(mt("[%k : %o:=%l || %m:=%n]"), mt("[9;P]")));
        assertEquals(mt("i1"), mc.instantiate(new SchemaVariable("%o", intTy)));
        assertEquals(mt("1"), mc.instantiate(new SchemaVariable("%l", intTy)));
        assertEquals(mt("b1"), mc.instantiate(new SchemaVariable("%m", bool)));
        assertEquals(mt("true"), mc.instantiate(new SchemaVariable("%n", bool)));
    }
    
    
    public void testTyping() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        mc.leftMatch(mt("%a"), mt("2"));
        
        assertEquals(mt("arb = 2"), mc.instantiate(mt("arb = %a as int")));
    }
    
    public void testDoubling() throws Exception {
        
        TermMatcher mc = new TermMatcher(env);
        assertFalse(mc.leftMatch(mt("g(arb as %'a, arb as %'a)"), mt("g(arb as int,arb as bool)")));
        mc = new TermMatcher(env);
        assertFalse(mc.leftMatch(mt("(\\T_all %'b; bf(%x as %'b))"), mt("(\\T_all 'a; bf(0))")));
        mc = new TermMatcher(env);
        assertFalse(mc.leftMatch(mt("(\\T_all %'a; true) & arb as %'a = arb"), mt("(\\T_all %'a; true) & arb as int = arb")));
        
    }
    
    public void testOccurCheck() throws Exception {
        TermMatcher mc = new TermMatcher(env);
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
        assertTrue(TermMatcher.containsSchemaVariables(mt("{ %c := 0 }true")));
        assertTrue(TermMatcher.containsSchemaVariables(mt("(\\forall %c; true)")));
        assertTrue(TermMatcher.containsSchemaVariables(mt("[%a]")));
        // from another one:
        assertTrue(TermMatcher.containsSchemaVariables(makeTerm("(\\forall i; %a > i)")));
    }
    
    public void testBinder() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        Term term = mt("(\\forall i; i > 0)");
        assertTrue(mc.leftMatch(mt("(\\forall %i; %b)"), term));
        assertEquals(Environment.getBoolType(), mc.getTypeFor("b"));
        assertEquals(Environment.getIntType(), mc.getTypeFor("i"));
        assertEquals(term.getSubterm(0), mc.getTermFor(new SchemaVariable("%b", Environment.getBoolType())));
        assertEquals(new Variable("i", Environment.getIntType()), 
                mc.getTermFor(new SchemaVariable("%i", Environment.getBoolType())));
        
        mc = new TermMatcher(env);
        assertFalse(mc.leftMatch(mt("(\\exists %i; %b)"), term));
    }
    
    public void testSchemaUpdates() throws Exception {
        TermMatcher mc = new TermMatcher(env);
        mc.leftMatch(mt("{U}%a"), mt("{i1:=0}b2"));
        
        assertEquals(mt("{i1:=0}i1"), mc.instantiate(mt("{U}i1")));
        assertEquals(mt("{V}b2"), mc.instantiate(mt("{V}%a")));
    }
    
    public void testTypeQuantification() throws Exception {
    
        TermMatcher mc = new TermMatcher(env);
        
        mc.leftMatch(mt("(\\T_all %'a; arb = arb as %'a)"), mt("(\\T_all 'a; arb = arb as 'a)"));
        assertEquals(new TypeVariable("a"), mc.getTypeFor("a"));
        
        mc = new TermMatcher(env);
        boolean res = mc.leftMatch(mt("(\\T_all %'a; (\\forall %x as %'a; %b))"), mt("(\\T_all 'a; (\\forall y as 'a; id(y)=y))"));
        assertTrue(res);
        
        assertFalse(mc.leftMatch(mt("(\\T_all %'b; true)"), mt("(\\T_all 'a; true as %'b)")));
        
        
    }
    
}
