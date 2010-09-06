package de.uka.iti.pseudo.util;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.TextInstantiator;

public class TestTextInstantiator extends TestCaseWithEnv {

    
    
    public void testStringInst() throws Exception {
        
        MutableRuleApplication mra = new MutableRuleApplication();
        TextInstantiator inst = new TextInstantiator(mra);
        
        Term t1 = makeTerm("1+2");
        Term t2 = makeTerm("[99; P]");
        mra.getSchemaVariableMapping().put("%x", t1);
        mra.getSchemaVariableMapping().put("%longerName", t2);
        
        String instantiated = inst.replaceInString("test {%x} and try {%longerName} but not {this} nor {%that}");
        
        assertEquals("test " + t1 + " and try " + t2 + " but not ?? nor ??", instantiated);
    }
    
    public void testStringInstWithPP() throws Exception {
        
        MutableRuleApplication mra = new MutableRuleApplication();
        TextInstantiator inst = new TextInstantiator(mra);
        
        PrettyPrint pp = new PrettyPrint(env);
        Term t1 = makeTerm("1+2");
        mra.getSchemaVariableMapping().put("%x", t1);
        
        String instantiated = inst.replaceInString("test {%x} with pp", pp);
        
        assertEquals("test 1 + 2 with pp", instantiated);
        
    }
    
    public void testExplainStringInst() throws Exception {
        MutableRuleApplication mra = new MutableRuleApplication();
        TextInstantiator inst = new TextInstantiator(mra);
        PrettyPrint pp = new PrettyPrint(env);
        Term t1 = makeTerm("[0;P]");
        mra.getSchemaVariableMapping().put("%x", t1);
        
        String instantiated = inst.replaceInString("test {explain %x}", pp);
        
        assertEquals("test first statement", instantiated);
        
    }
    
    public void testProperty() throws Exception {
        MutableRuleApplication mra = new MutableRuleApplication();
        TextInstantiator inst = new TextInstantiator(mra);
        mra.getProperties().put("testcase", "helloWorld");
        mra.getProperties().put("with spaces", "contains space");
        
        String instantiated = inst.replaceInString("test {property testcase} and '{property with spaces}'");
        
        assertEquals("test helloWorld and 'contains space'", instantiated);
        assertEquals("unknown ??", inst.replaceInString("unknown {property unknown}"));
    }
}
