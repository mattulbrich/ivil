package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

public class TestTermInstantiator extends TestCaseWithEnv {

    // from a bug
    public void testInstantiateTerm() throws Exception {
        
        Term orig = makeTerm("(\\exists %x; %x = 5)");
        
        TermInstantiator inst = new TermInstantiator();
        inst.getTermMap().put("%x", new Variable("xx", Environment.getIntType()));
        
        assertEquals(makeTerm("(\\exists xx; xx = 5)"), inst.instantiate(orig));
        
    }
    
    public void testInUpdates() throws Exception {
        
        Term orig = makeTerm("{ %c := %d+1}true");
        TermInstantiator inst = new TermInstantiator();
        inst.getTermMap().put("%c", makeTerm("i1"));
        inst.getTermMap().put("%d", makeTerm("22"));
        System.out.println("{ i1 := $plus(22,1) }true");
        System.out.println(inst.instantiate(orig));
        assertEquals(makeTerm("{ i1 := $plus(22,1) }true"), inst.instantiate(orig));
    }

    // i2 is not assignable
    public void testInUpdates2() throws Exception {
        
        Term orig = makeTerm("{ %c := 0 }true");
        TermInstantiator inst = new TermInstantiator();
        inst.getTermMap().put("%c", makeTerm("i2"));
        
        try {
            inst.instantiate(orig);
            fail("i2 is not assignable - should have failed");
        } catch (Exception e) {
        }
    }
    
    public void testStringInst() throws Exception {
        
        TermInstantiator inst = new TermInstantiator();
        Term t1 = makeTerm("1+2");
        Term t2 = makeTerm("[99]");
        inst.getTermMap().put("%x", t1);
        inst.getTermMap().put("%longerName", t2);
        
        String instantiated = inst.replaceInString("test {%x} and try {%longerName} but not {this} nor {%that}");
        
        assertEquals("test " + t1 + " and try " + t2 + " but not ?? nor ??", instantiated);
    }
    
    public void testProgram() throws Exception {
        TermInstantiator inst = new TermInstantiator();
        
        inst.getTermMap().put("%x", makeTerm("i1"));
        inst.getTermMap().put("%v", makeTerm("2"));
        
        inst.getTypeMapper().leftUnify(new TypeVariable("%v"), Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("{%x:=%v}i2"));
        assertEquals(makeTerm("{ i1 := 2 }i2"), t);
    }

}
