package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Variable;

public class TestTermInstantiator extends TestCaseWithEnv {

    // from a bug
    public void testInstantiateTerm() throws Exception {
        
        Term orig = makeTerm("(\\exists %x; %x = 5)");
        
        TermInstantiator inst = new TermInstantiator();
        inst.getTermMap().put("%x", new Variable("xx", Environment.getIntType()));
        
        assertEquals(makeTerm("(\\exists xx; xx = 5)"), inst.instantiate(orig));
        
    }


}
