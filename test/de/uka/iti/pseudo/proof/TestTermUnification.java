package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class TestTermUnification extends TestCaseWithEnv {
    
    private static Term mt(String s) throws Exception {
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
     */  
    public void testModalities() throws Exception {
        TermUnification mc = new TermUnification(env);
        
        assertFalse(mc.leftUnify(mt("[%a : assert %b]"), mt("[0]")));
        assertTrue(mc.leftUnify(mt("[%a : assert %b]"), mt("[1]")));
        assertFalse(mc.leftUnify(mt("[%a]"), mt("[2]")));
        assertEquals(mt("b2"), mc.instantiate(new SchemaVariable("%b", Environment.getBoolType())));

        assertTrue(mc.leftUnify(mt("[%c : %x = %v]"), mt("[5]")));
        assertEquals(mt("i1"), mc.instantiate(new SchemaVariable("%x", Environment.getBoolType())));
        assertEquals(mt("i2+i3"), mc.instantiate(new SchemaVariable("%v", Environment.getBoolType())));
        
        assertFalse(mc.leftUnify(mt("[[%d]]"), mt("[7]")));
        
        assertTrue(mc.leftUnify(mt("[%e]"), mt("[6]")));
        assertTrue(mc.leftUnify(mt("[%e]"), mt("[7]")));
    }
    
    
    public void testTyping() throws Exception {
        TermUnification mc = new TermUnification(env);
        mc.leftUnify(mt("%a"), mt("2"));
        
        assertEquals(mt("arb = 2"), mc.instantiate(mt("arb = %a as int")));
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
    
    // TODO Test binders!
}
