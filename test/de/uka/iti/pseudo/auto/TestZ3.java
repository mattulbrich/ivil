package de.uka.iti.pseudo.auto;

import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

public class TestZ3 extends TestCaseWithEnv {

    public void testSolve() throws Exception {
        
        Term t = makeTerm("arb as bool");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();
        
        z3.solve(s, env);
    }

}
