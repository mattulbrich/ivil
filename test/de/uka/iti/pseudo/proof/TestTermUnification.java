package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class TestTermUnification extends TestCaseWithEnv {
    
    private static Term mt(String s) throws Exception {
        return makeTerm(s);
    }
    
//    public void testSchemas() throws Exception {
//        TermUnification mc = new TermUnification();
//        
//        Term t1 = mt("%a as int");
//        mc.leftUnify(t1, t1);
//        
//        Term t2 = mt("%a as 'a");
//        mc.leftUnify(t2, t2);
//        
//        assertEquals(t1, mc.instantiate(t2));
//        
//        Term t3 = mt("%a as bool");
//        if(mc.leftUnify(t3, t3)) {
//            fail("should have failed but didnt: " + t3);
//        }
//        
//    }
    
    public void testLeftUnify1() throws Exception {
        TermUnification mc = new TermUnification();
        
        Term t1 = mt("%a");
        Term t2 = mt("2+2");
        boolean res = mc.leftUnify(t1, t2);
        assertTrue(res);
        assertEquals(t2, mc.instantiate(t1));
        
        mc = new TermUnification();
        res = mc.leftUnify(mt("%a + %i"), mt("2 + 3"));
        assertTrue(res);
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getIntType())));
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getBoolType())));
        assertEquals(mt("3"), mc.instantiate(mt("%i")));
    }
    
    // from an early bug
    public void testInstantiateConst() throws Exception {
        TermUnification mc = new TermUnification();
        assertEquals(mt("true"), mc.instantiate(mt("true")));
    }
    
    public void testDiffInst() throws Exception {
        TermUnification mc = new TermUnification();
        
        assertFalse(mc.leftUnify(mt("%a + %a"), mt("2+3")));
    }
    
    public void testConsecutiveUnification() throws Exception {
        TermUnification mc = new TermUnification();
        
        assertTrue(mc.leftUnify(mt("%i"), mt("2")));
        assertTrue(mc.leftUnify(mt("%i1 + %i"), mt("4 + 2")));
        assertFalse(mc.leftUnify(mt("%i2 + %i"), mt("5 + 3")));
        // %i2 must not have been bound yet.
        assertTrue(mc.leftUnify(mt("%i2"), mt("7")));
    }

    
    public void testModalities() throws Exception {
        TermUnification mc = new TermUnification();
        
        assertTrue(mc.leftUnify(mt("[ &a ; &b]b1"), mt("[i1:=1 ; i1:=2]b1")));
        assertFalse(mc.leftUnify(mt("[ &c ; &c]b1"), mt("[i1:=1 ; i1:=2]b1")));
        assertTrue(mc.leftUnify(mt("[ &a ; &a]b1"), mt("[i1:=1 ; i1:=1]b1")));
        
        // ensure that &a is now bound ...
        assertFalse(mc.leftUnify(mt("[ &a ]b1"), mt("[ i1:=0 ]b1")));
        
        // and &c is not
        assertTrue(mc.leftUnify(mt("[ &c ]b1"), mt("[ i1:=0 ]b1")));
    }
    
    // extracted from a bug
    public void testMatchIfThenElse() throws Exception {
        TermUnification mc = new TermUnification();
        
        assertTrue(mc.leftUnify(makeTerm("[if %b then &a else &a end]true"), makeTerm("[if b1 then skip else skip end]true")));
        assertFalse(mc.leftUnify(makeTerm("[if %b then &a end]true"), makeTerm("[if b1 then skip else skip end]true")));
    }
    
    
    public void testTyping() throws Exception {
        TermUnification mc = new TermUnification();
        mc.leftUnify(mt("%a"), mt("2"));
        
        assertEquals(mt("arb = 2"), mc.instantiate(mt("arb = %a as int")));
    }
    
    public void testOccurCheck() throws Exception {
        TermUnification mc = new TermUnification();
        try {
            mc.addInstantiation(new SchemaVariable("%a", Environment
                    .getIntType()), mt("%a + 2"));
            fail("should fail");
        } catch (TermException e) {
            // should fail
        }
    }
    
    public void testWhileInv() throws Exception {
        TermUnification mc = new TermUnification();
        
        assertTrue(mc.leftUnify(makeTerm("[while %c inv %inv1 do &a end]true"), makeTerm("[while b1 inv b2 do skip end]true")));
        assertEquals(mt("b2 as bool"), mc.instantiate(mt("%inv1 as bool")));
        
        assertTrue(mc.leftUnify(makeTerm("[while %c inv %inv2 do &a end]true"), makeTerm("[while b1 do skip end]true")));
        assertEquals(mt("true"), mc.instantiate(mt("%inv2 as bool")));
        
        assertTrue(mc.leftUnify(makeTerm("[while %c do &a end]true"), makeTerm("[while b1 inv b2 do skip end]true")));
        assertTrue(mc.leftUnify(makeTerm("[while %c do &a end]true"), makeTerm("[while b1 do skip end]true")));
        
    }
    
    // TODO Test binders!
}
