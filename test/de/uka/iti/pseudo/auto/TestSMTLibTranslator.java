package de.uka.iti.pseudo.auto;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestSMTLibTranslator extends TestCaseWithEnv {

    public void testQuantifiers() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        trans.indention = -1;
        
        Term t = makeTerm("(\\forall x; x > 0)");
        assertEquals("(forall (?x Int) (> ?x 0))", trans.translate(t));
        
        t = makeTerm("(\\exists y; arb <= y)");
        assertEquals("(exists (?y Int) (<= extra.arb..Int ?y))", trans.translate(t));
    }
    
    public void testArithmetic() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        trans.indention = -1;
        
        String[] ops = { "+", "-", "<", "<=", ">", ">=" };
        
        for (String op : ops) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t));
        }
    }
    
    public void testUnknown() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        trans.indention = -1;
        
        assertEquals("unknown0", trans.translate(makeTerm("[0;P]")));
        assertEquals("unknown1", trans.translate(makeTerm("{i1:=0}i2")));
        assertEquals("unknown2", trans.translate(makeTerm("{i1:=0}b2")));
        
        assertTrue(trans.extrapreds.contains("(unknown0)"));
        assertTrue(trans.extrafuncs.contains("(unknown1 Int)"));
        assertTrue(trans.extrapreds.contains("(unknown2)"));
    }
    
    public void testUninterpreted() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        trans.indention = -1;
        
        assertEquals("extra.arb..poly_int_poly_int_int__", trans.translate(makeTerm("arb as poly(int, poly(int,int))")));
        assertEquals("(extra.f.Int.Int 5)", trans.translate(makeTerm("f(5) as int")));
        assertEquals("extra.arb..bool", trans.translate(makeTerm("arb as bool")));
        
    }
    
}
