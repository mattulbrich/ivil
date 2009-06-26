package de.uka.iti.pseudo.auto;

import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class TestZ3 extends TestCaseWithEnv {

    public void testSolveUnsat() throws Exception {
        Term t = makeTerm("(\\forall x; x > 0) -> (\\forall x; x >= 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();
        
        try {
            Pair<Result, String> res = z3.solve(s, env, 100);
            assertEquals(Result.VALID, res.fst());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testSolveSat() throws Exception {
        Term t = makeTerm("(\\forall x; x >= 0) -> (\\forall x; x > 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();
        
        try {
            Pair<Result, String> res = z3.solve(s, env, 100);
            assertEquals(Result.NOT_VALID, res.fst());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
