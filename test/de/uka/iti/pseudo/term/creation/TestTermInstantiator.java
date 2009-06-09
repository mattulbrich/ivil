package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.AssignModality;
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
    
    // from a bug
    public void testInModalities() throws Exception {
        
        Term orig = makeTerm("[if %c then skip else skip end]true");
        TermInstantiator inst = new TermInstantiator();
        inst.getTermMap().put("%c", makeTerm("false"));
        
        assertEquals(makeTerm("[if false then skip else skip end]true"), inst.instantiate(orig));

    }
    
    public void testStringInst() throws Exception {
        
        TermInstantiator inst = new TermInstantiator();
        Term t1 = makeTerm("1+2");
        inst.getTermMap().put("%x", t1);
        AssignModality m2 = new AssignModality(env.getFunction("i1"), makeTerm("2"));
        inst.getModalityMap().put("&a", m2);
        
        String instantiated = inst.replaceInString("test {%x} and try {&a} but not {this} nor {%that}");
        
        assertEquals("test " + t1 + " and try " + m2 + " but not ?? nor ??", instantiated);
    }
    
    public void testProgram() throws Exception {
        TermInstantiator inst = new TermInstantiator();
        
        inst.getTermMap().put("%x", makeTerm("i1"));
        inst.getTermMap().put("%v", makeTerm("2"));
        
        inst.getTypeMapper().leftUnify(new TypeVariable("%v"), Environment.getIntType());
        
        Term t = inst.instantiate(makeTerm("[%x:=%v]i2"));
        assertEquals(makeTerm("[i1:=2]i2"), t);
    }

}
