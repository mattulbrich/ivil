package de.uka.iti.pseudo.auto;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestZ3Translator extends TestCaseWithEnv {

    public void testZ3() throws Exception {
        
        Z3Translator trans = new Z3Translator(env);
        
        Term t = makeTerm("(\\forall x; x > 0)");
        t.visit(trans);
    }
    
}
