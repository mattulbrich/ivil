package de.uka.iti.pseudo.proof;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ParseException;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class TestTermUnification extends TestCase {
    
    private Environment env;
    
    public TestTermUnification() throws Exception {
        env = TestTermParser.loadEnv();
    }
    
    private Term mt(String s) throws ParseException, ASTVisitException {
        return TermMaker.makeTerm(s, env);
    }

    public void testLeftUnify1() throws Exception {
        TermUnification mc = new TermUnification();
        
        Term t1 = TermMaker.makeTerm("%a", env);
        Term t2 = TermMaker.makeTerm("2+2", env);
        boolean res = mc.leftUnify(t1, t2);
        assertTrue(res);
        assertEquals(t2, mc.instantiate(t1));
        
        mc = new TermUnification();
        res = mc.leftUnify(mt("%a + %b"), mt("2 + 3"));
        assertTrue(res);
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getIntType())));
        assertEquals(mt("2"), mc.getTermFor(new SchemaVariable("%a", Environment.getBoolType())));
        assertEquals(mt("3"), mc.instantiate(mt("%b")));
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
        
        assertTrue(mc.leftUnify(mt("%a"), mt("2")));
        assertTrue(mc.leftUnify(mt("%b+%a"), mt("4 + 2")));
        assertFalse(mc.leftUnify(mt("%c+%a"), mt("5 + 3")));
        // %c must not have been bound yet.
        assertTrue(mc.leftUnify(mt("%c"), mt("7")));
    }

    
    public void testModalities() throws Exception {
        TermUnification mc = new TermUnification();
        
        assertTrue(mc.leftUnify(mt("[ &a ; &b]b1"), mt("[i1:=1 ; i1:=2]b1")));
        assertFalse(mc.leftUnify(mt("[ &a ; &a]b1"), mt("[i1:=1 ; i1:=2]b1")));
        assertTrue(mc.leftUnify(mt("[ &a ; &a]b1"), mt("[i1:=1 ; i1:=1]b1")));
    }
    
    // TODO Test binders!
}
