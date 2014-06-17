/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.util.Collections;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

//
// some of these results used to "NOT_VALID"
// due to the complex translation now, this cannot be guaranteed any longer.
// Unfortunately.
//

public class TestZ3 extends TestCaseWithEnv {

    private static Map<String, String> PROP1000 = Collections.singletonMap("timeout", "1000");
    private static Map<String, String> PROP2000 = Collections.singletonMap("timeout", "2000");

    public void testSolveUnsatSMT() throws Exception {
        Term t = makeTerm("(\\forall x; x > 0 -> x >= 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, PROP1000);
        assertEquals(Result.VALID, res.fst());
    }

    public void testSolveSatSMT() throws Exception {
        Term t = makeTerm("(\\forall x; x >= 0 ->  x > 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, PROP1000);
        // NOT SAME
        assertNotSame(Result.VALID, res.fst());
    }

    public void testSolveExFalsoSMT() throws Exception {
        Term t1 = makeTerm("b1");
        Term t2 = makeTerm("!b1");
        Term t3 = makeTerm("b2");
        Z3SMT z3 = new Z3SMT();
        Sequent s = new Sequent(new Term[] { t1, t2 }, new Term[] { t3 });

        Pair<Result, String> res = z3.solve(s, env, PROP1000);
        assertSame(Result.VALID, res.fst());
    }

    public void testSolveGt0SMT() throws Exception {
        Term t = makeTerm("(\\forall x; x>5)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, PROP1000);
        assertNotSame(Result.VALID, res.fst());
    }

//    Cannot reproduce because of persistent cache :-(
//    public void testSolveNoCacheUnsuccess() throws Exception {
//        Term t = makeTerm("(\\forall x as int; bf(x)) -> bf(id(42))");
//        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
//        Z3SMT z3 = new Z3SMT();
//
//        Map<String, String> timeout1sec = Collections.singletonMap("timeout", "0");
//        Pair<Result, String> res = z3.solve(s, env, timeout1sec);
//        assertSame(Result.UNKNOWN, res.fst());
//
//        res = z3.solve(s, env, PROP2000);
//        assertSame(Result.VALID, res.fst());
//    }

    public void testSolveExistsSMT() throws Exception {
        Term t = makeTerm("(\\exists x; x>5)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, PROP1000);
        assertEquals(Result.VALID, res.fst());
    }

}
