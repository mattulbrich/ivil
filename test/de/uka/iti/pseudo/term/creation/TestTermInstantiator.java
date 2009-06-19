package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;

public class TestTermInstantiator extends TestCaseWithEnv {
    
    Map<String, Term> termmap;
    Map<String, Type> typemap;
    private TermInstantiator inst;
    
    protected void setUp() throws Exception {
        termmap = new HashMap<String, Term>();
        typemap = new HashMap<String, Type>();
        inst = new TermInstantiator(termmap, typemap);
    }

    // from a bug
    public void testInstantiateTerm() throws Exception {
        
        Term orig = makeTerm("(\\exists %x; %x = 5)");
        
        termmap.put("%x", new Variable("xx", Environment.getIntType()));
        
        assertEquals(makeTerm("(\\exists xx; xx = 5)"), inst.instantiate(orig));
        
    }
    
    public void testInUpdates() throws Exception {
        
        Term orig = makeTerm("{ %c := %d+1}true");
        termmap.put("%c", makeTerm("i1"));
        termmap.put("%d", makeTerm("22"));
        System.out.println("{ i1 := $plus(22,1) }true");
        System.out.println(inst.instantiate(orig));
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
    
    public void testStringInst() throws Exception {
        
        Term t1 = makeTerm("1+2");
        Term t2 = makeTerm("[99]");
        termmap.put("%x", t1);
        termmap.put("%longerName", t2);
        
        String instantiated = inst.replaceInString("test {%x} and try {%longerName} but not {this} nor {%that}");
        
        assertEquals("test " + t1 + " and try " + t2 + " but not ?? nor ??", instantiated);
    }
    
    // partially from a bug
    public void testUpdate() throws Exception {
        
        termmap.put("%x", makeTerm("i1"));
        termmap.put("%v", makeTerm("2"));
        typemap.put("%v", Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("{%x:=%v}i2"));
        assertEquals(makeTerm("{ i1 := 2 }i2"), t);
        
        t = inst.instantiate(makeTerm("{%x:=%v}%x"));
        assertEquals(makeTerm("{ i1 := 2 }i1"), t);
    }

    public void testSchemaProgram() throws Exception {
        
        termmap.put("%a", makeTerm("[2]"));
        
        Term t = inst.instantiate(makeTerm("%a as bool"));
        assertEquals(makeTerm("[2]"), t);
        
        t = inst.instantiate(makeTerm("[%a]"));
        assertEquals(makeTerm("[2]"), t);
        
        try {
            t = inst.instantiate(makeTerm("[[%a]]"));
            fail("wrong termination - should have failed");
        } catch (Exception e) {
        }
    }
    
    // due to problems
    // [1] is assert b2 
    public void testProgramComparingInstantiation() throws Exception {
        
        inst = new ProgramComparingTermInstantiator(termmap, typemap, env);
        termmap.put("%a", makeTerm("[1]"));
        termmap.put("%b", makeTerm("b2"));
        
        // it suffices if it does not fail
        inst.instantiate(makeTerm("[%a : assert b2]"));
        inst.instantiate(makeTerm("[%a : assert %b]"));
        inst.instantiate(makeTerm("[%a]"));
        
        try {
            inst.instantiate(makeTerm("[%a : assume b2]"));
            fail("should have failed");
        } catch (Exception e) {
        }
        
        try {
            inst.instantiate(makeTerm("[%a : assert b1]"));
            fail("should have failed");
        } catch (Exception e) {
        }
        
        try {
            inst.instantiate(makeTerm("[%a : assert %c]"));
            fail("should have failed");
        } catch (Exception e) {
        }
    }
}
