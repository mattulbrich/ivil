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

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class TestZ3 extends TestCaseWithEnv {

    public void testSolveUnsat() throws Exception {
        Term t = makeTerm("(\\forall x; x > 0 -> x >= 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.VALID, res.fst());
    }
    
    public void testSolveUnsatSMT() throws Exception {
        Term t = makeTerm("(\\forall x; x > 0 -> x >= 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.VALID, res.fst());
    }

    public void testSolveSat() throws Exception {
        Term t = makeTerm("(\\forall x; x >= 0 ->  x > 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.NOT_VALID, res.fst());
    }
    
    public void testSolveSatSMT() throws Exception {
        Term t = makeTerm("(\\forall x; x >= 0 ->  x > 0)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.NOT_VALID, res.fst());
    }

    public void testSolveExFalso() throws Exception {
        Term t1 = makeTerm("b1");
        Term t2 = makeTerm("!b1");
        Term t3 = makeTerm("b2");
        Z3 z3 = new Z3();
        Sequent s = new Sequent(new Term[] { t1, t2 }, new Term[] { t3 });

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.VALID, res.fst());
    }
    
    public void testSolveExFalsoSMT() throws Exception {
        Term t1 = makeTerm("b1");
        Term t2 = makeTerm("!b1");
        Term t3 = makeTerm("b2");
        Z3SMT z3 = new Z3SMT();
        Sequent s = new Sequent(new Term[] { t1, t2 }, new Term[] { t3 });

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.VALID, res.fst());
    }

    public void testSolveGt0() throws Exception {
        Term t = makeTerm("(\\forall x; x>5)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.NOT_VALID, res.fst());
    }
    
    public void testSolveGt0SMT() throws Exception {
        Term t = makeTerm("(\\forall x; x>5)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.NOT_VALID, res.fst());
    }
    
    public void TODO_testSolveExists() throws Exception {
        Term t = makeTerm("(\\exists x; x>5)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3 z3 = new Z3();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.VALID, res.fst());
    }
    
    public void testSolveExistsSMT() throws Exception {
        Term t = makeTerm("(\\exists x; x>5)");
        Sequent s = new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(t));
        Z3SMT z3 = new Z3SMT();

        Pair<Result, String> res = z3.solve(s, env, 1000);
        assertEquals(Result.VALID, res.fst());
    }

}
