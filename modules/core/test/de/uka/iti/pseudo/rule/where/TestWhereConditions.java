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
package de.uka.iti.pseudo.rule.where;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.FilterRuleApplication;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermMatcher;

public class TestWhereConditions extends TestCaseWithEnv {

    public void testProgramFree() throws Exception {

        ProgramFree pf = new ProgramFree();
        assertTrue(pf.check(null, new Term[] { makeTerm("1+2 = 3") }, null, env));
        assertFalse(pf.check(null, new Term[] { makeTerm("true & [5;P]true") }, null, env));

    }

    public void testIntLiteral() throws Exception {
        IntLiteral intLit = new IntLiteral();

        assertTrue(intLit.check(null, new Term[] { makeTerm("22") }, null, env));
        assertFalse(intLit.check(null, new Term[] { makeTerm("2+2") }, null, env));
    }

    public void testDistinctAssumeAndFind() throws Exception {
        DistinctAssumeAndFind daf = new DistinctAssumeAndFind();

        MutableRuleApplication ra = new MutableRuleApplication();
        ra.setFindSelector(new TermSelector("A.1.1"));
        ra.getAssumeSelectors().add(new TermSelector("A.0"));
        assertTrue(daf.check(null, null, ra, DEFAULT_ENV));
    }

    public void testCanEval() throws Exception {
        CanEvaluateMeta can = new CanEvaluateMeta();

        // Meta evaluator needs a working rule app.
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        Proof p = new Proof(Environment.getTrue(), env);
        ram.setProofNode(p.getRoot());
        // the following does not allows for skolemisation!
        RuleApplication fix = new FilterRuleApplication(ram) {
            @Override
            public Map<String, String> getProperties() {
                return Collections.emptyMap();
            }
            @Override
            public boolean hasMutableProperties() { return false; };
        };

        assertTrue(can.check(null, new Term[] { makeTerm("$$intEval(1+1)") }, ram, env));
        assertFalse(can.check(null, new Term[] { makeTerm("$$intEval(i1+1)") }, ram, env));
        assertTrue(can.check(null, new Term[] { makeTerm("$$skolem(1)") }, ram, env));
        assertFalse(can.check(null, new Term[] { makeTerm("$$skolem(1)") }, fix, env));
    }

    public void testFreshVar() throws Exception {
        FreshVariable fresh = new FreshVariable();

        assertTrue(fresh.check(null, new Term[] { makeTerm("\\var x as int"), makeTerm("(\\forall x as int; true)")}, null, env));
        assertFalse(fresh.check(null, new Term[] { makeTerm("\\var x as int"), makeTerm("\\var x + 2")}, null, env));

        TermMatcher tm = new TermMatcher();
        fresh.addInstantiations(tm, new Term[] { makeTerm("%x as int"), makeTerm("\\var x > 0"), makeTerm("(\\forall x1; x1 > 0)") });
        Term result = tm.getTermInstantiation().get("%x");
        assertEquals(makeTerm("\\var x2 as int"), result);
        assertTrue(fresh.check(null, new Term[] { result, makeTerm("\\var x > 0"), makeTerm("(\\forall x1; x1 > 0)") }, null, env));
    }

    public void testFreshVarInstType() throws Exception {
        FreshVariable fresh = new FreshVariable();

        TermMatcher tm = new TermMatcher();
        tm.addTypeInstantiation("x", Environment.getIntType());
        fresh.addInstantiations(tm, new Term[] { makeTerm("%x as %'x") });
        Term result = tm.instantiate(makeTerm("%x as %'x"));
        assertEquals(makeTerm("\\var x as int"), result);

    }

    public void testCompatibleTypes() throws Exception {

        CompatibleTypes ct = new CompatibleTypes();

        assertTrue(ct.check(null, new Term[] { makeTerm("3"), makeTerm("5") }, null, env));
        assertTrue(ct.check(null, new Term[] { makeTerm("type as 'b"), makeTerm("type as 'a") }, null, env));
        assertTrue(ct.check(null, new Term[] { makeTerm("type as set('b)"), makeTerm("type as 'a") }, null, env));
        assertTrue(ct.check(null, new Term[] {
                makeTerm("type as poly('a, bool)"),
                makeTerm("type as poly(int, 'b)") }, null, env));

        assertFalse(ct.check(null, new Term[] { makeTerm("3"), makeTerm("true") }, null, env));
        assertFalse(ct.check(null, new Term[] { makeTerm("type as set('b)"), makeTerm("type as poly('a,'a)") }, null, env));
        assertFalse(ct.check(null, new Term[] {
                makeTerm("type as poly('a, bool)"),
                makeTerm("type as poly(int, 'a)") }, null, env));

        try {
            ct.check(null, new Term[] { makeTerm("type as %'a"), makeTerm("type as 'a") }, null, env);
            fail("Should have failed because of the schema type");
        } catch (RuleException e) {
            out(e);
        }

    }

    public void testFreshTypeVar() throws Exception {
        FreshTypeVariable fresh = new FreshTypeVariable();

        // we need some function symbol ...
        env = new Environment("none:temp", env);
        env.addFunction(new Function("emptyset",
                TermMaker.makeType("set('a)", new SymbolTable(env)),
                new Type[0], false, false, ASTLocatedElement.CREATED));

        assertTrue(fresh.check(null, new Term[] { makeTerm("arb as 'a"), makeTerm("(\\T_all 'b; true)")}, null, env));
        assertFalse(fresh.check(null, new Term[] { makeTerm("type as 'a"), makeTerm("(\\T_all 'a; true)")}, null, env));
        assertFalse(fresh.check(null, new Term[] { makeTerm("%x as 'a"), makeTerm("emptyset as set('a)")}, null, env));

        TermMatcher tm = new TermMatcher();
        fresh.addInstantiations(tm, new Term[] {
                makeTerm("%a as %'a"),
                makeTerm("(\\T_all 'a; (\\T_all 'a1; true))"),
                makeTerm("emptyset as set('a2)") });

        Type result = tm.getTypeInstantiation().get("a");
        assertEquals(TypeVariable.getInst("a3"), result);

        assertTrue(fresh.check(null, new Term[] {
                makeTerm("arb as 'a3"),
                makeTerm("(\\T_all 'a; (\\T_all 'a1; true))"),
                makeTerm("emptyset as set('a2)")  }, null, env));
    }

    // was a bug
    public void testVarNested() throws Exception {
        FreshVariable fresh = new FreshVariable();
        assertTrue(fresh.check(null, new Term[] { makeTerm("\\var x as int"), makeTerm("(\\forall x as int; (\\forall x as int; true) & x>0)")}, null, env));

        NoFreeVars nofree = new NoFreeVars();
        assertTrue(nofree.check(null, new Term[] { makeTerm("(\\forall x as int; (\\forall x as int; true) & x>0)") }, null, env));
    }

    public void testInteractiveSyntax() throws Exception {
        Interactive inter = new Interactive();
        Term intX = makeTerm("%x as int");
        Term alphaX = makeTerm("%x as %'x");

        inter.checkSyntax(new Term[] { intX });
        inter.checkSyntax(new Term[] { intX, Environment.getFalse() });
        inter.checkSyntax(new Term[] { alphaX, Environment.getTrue() });
        inter.checkSyntax(new Term[] { intX, Environment.getFalse() });

        try {
            inter.checkSyntax(new Term[] { intX, Environment.getTrue() });
            fail("should complain that x has not a schema type");
        } catch(RuleException ex) {
            if(VERBOSE) {
                ex.printStackTrace();
            }
        }

        try {
            inter.checkSyntax(new Term[] { intX, intX });
            fail("should complain that 2nd arg has wrong kind");
        } catch(RuleException ex) {
            if(VERBOSE) {
                ex.printStackTrace();
            }
        }

        try {
            inter.checkSyntax(new Term[] { });
            fail("should complain that no args");
        } catch(RuleException ex) {
            if(VERBOSE) {
                ex.printStackTrace();
            }
        }

        try {
            inter.checkSyntax(new Term[] { Environment.getFalse()});
            fail("should complain that 1st argument not schema");
        } catch(RuleException ex) {
            if(VERBOSE) {
                ex.printStackTrace();
            }
        }

    }

//    // was a bug
//    // originally "where interactive %x as %'x, true"
//    // but "%'x" was instantiated to "int"
//    public void testInteractiveInstantiated() throws Exception {
//        Interactive inter = new Interactive();
//        Term[] formal = {
//                makeTerm("%x as %'x"),
//                Environment.getTrue() };
//        Term[] actual = {
//                makeTerm("%x as int"),
//                Environment.getTrue() };
//
//        inter.checkSyntax(formal);
//        RuleApplication ra = new MutableRuleApplication();
//        inter.check(formal, actual, ra, env);
//        assertEquals(Collections.singletonMap(Interactive.INTERACTION + "(%x)",
//                    Interactive.INSTANTIATE_PREFIX + "%'x"),
//                ra.getProperties());
//    }

    public void testNoFree() throws Exception {

        env = makeEnv("include \"$int.p\" " +
                "function int i1 " +
                "program Q assert true");

        NoFreeVars noFree = new NoFreeVars();

        assertFalse(checkNoFree(noFree, "\\var x as int"));
        assertTrue(checkNoFree(noFree, "i1 as int"));
        assertTrue(checkNoFree(noFree, "(\\forall x; \\var x > 0)"));
        assertFalse(checkNoFree(noFree, "(\\forall x; \\var x > 0) & \\var x < 0"));
        assertTrue(checkNoFree(noFree, "[0;Q]true"));
        assertFalse(checkNoFree(noFree, "[0;Q](\\var bb)"));
    }

    private boolean checkNoFree(NoFreeVars noFree, String s) throws RuleException,
            Exception {
        return noFree.check(null, new Term[] { makeTerm(s) }, null, null);
    }

    public void testToplevel() throws Exception {

        TopLevel tl = new TopLevel();

        assertFalse(checkToplevel(tl, "{i1:=1}b1", new SubtermSelector(0)));
        assertTrue(checkToplevel(tl, "{i1:=1}b1", new SubtermSelector(1)));
        assertFalse(checkToplevel(tl, "[0;P]b1", new SubtermSelector(0)));
        assertFalse(checkToplevel(tl, "[[0;P]](true -> (true -> (b1)))", new SubtermSelector(0,1,1)));
        assertTrue(checkToplevel(tl, "(true -> (true -> (b1))) & b1", new SubtermSelector(0,1,1)));

        assertFalse(checkToplevel(tl, "b1 -> (b1 -> [[0;P]]b1)", new SubtermSelector(1,1,0)));
        assertTrue(checkToplevel(tl, "b1 -> (b1 -> [[0;P]]b1)", new SubtermSelector(1,1)));
        assertTrue(checkToplevel(tl, "b1 -> (b1 -> [[0;P]]b1)", new SubtermSelector(1,0)));

    }

    private boolean checkToplevel(TopLevel tl, String string,
            SubtermSelector subtermSelector) throws Exception {
        Term term = makeTerm(string);
        TermSelector termSel = new TermSelector(new TermSelector("S.0"), subtermSelector);

        return tl.check(termSel, new Sequent(Collections.<Term>emptyList(), Arrays.asList(term)));
    }

}
