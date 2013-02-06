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

import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.BOOL;
import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.INT;
import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.UNIVERSE;
import junit.framework.AssertionFailedError;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;

public class TestSMTLib2Translator extends TestCaseWithEnv {

    public TestSMTLib2Translator() {
        System.err.println("New constructor");
    }

    public void testQuantifiers() throws Exception {

        SMTLib2Translator trans = new SMTLib2Translator(env);

        Term t = makeTerm("(\\forall x; x > 0)");
        assertEquals("(forall ((?Int.x Int)) (> ?Int.x 0))",
                trans.translate(t, BOOL));

        t = makeTerm("(\\forall x as 'a; x = x)");
        assertEquals("(forall ((?Universe.x Universe)) " +
                "(! (implies (and (ty ?Universe.x tyvar.a))" +
                " (= ?Universe.x ?Universe.x)) :pattern ((ty ?Universe.x tyvar.a))))",
                trans.translate(t, BOOL));

        t = makeTerm("(\\exists y; 0 <= y)");
        assertEquals("(exists ((?Int.y Int)) (<= 0 ?Int.y))",
                trans.translate(t, BOOL));

        t = makeTerm("(\\exists x as 'a; x = x)");
        assertEquals("(exists ((?Universe.x Universe)) " +
                "(! (and (and (ty ?Universe.x tyvar.a))" +
                " (= ?Universe.x ?Universe.x)) :pattern ((ty ?Universe.x tyvar.a))))",
                trans.translate(t, BOOL));
    }


    public void test2NestedQuantifier() throws Exception {

        SMTLib2Translator trans = new SMTLib2Translator(env);

        Term t = makeTerm("(\\forall x; (\\forall y; x > y))");
        assertEquals("(forall ((?Int.x Int) (?Int.y Int)) (> ?Int.x ?Int.y))",
                trans.translate(t, BOOL));

        t = makeTerm("(\\exists x; (\\exists y; x > y))");
        assertEquals("(exists ((?Int.x Int) (?Int.y Int)) (> ?Int.x ?Int.y))",
                trans.translate(t, BOOL));

        t = makeTerm("(\\exists x; (\\forall y; x > y))");
        assertEquals("(exists ((?Int.x Int)) (forall ((?Int.y Int)) (> ?Int.x ?Int.y)))",
                trans.translate(t, BOOL));

        env = makeEnv("sort S function bool p(S,S)");
        trans = new SMTLib2Translator(env);
        // Universe quantifications
        t = makeTerm("(\\forall x; (\\forall y; p(x,y)))");
        assertEquals("(forall ((?Universe.x Universe) (?Universe.y Universe)) " +
                "(! (implies (and (ty ?Universe.x ty.S) (ty ?Universe.y ty.S)) " +
                "(fct.p ?Universe.x ?Universe.y)) " +
                ":pattern ((ty ?Universe.x ty.S) (ty ?Universe.y ty.S))))",
                trans.translate(t, BOOL));

        t = makeTerm("(\\exists x; (\\exists y; p(x,y)))");
        assertEquals("(exists ((?Universe.x Universe) (?Universe.y Universe)) " +
                "(! (and (and (ty ?Universe.x ty.S) (ty ?Universe.y ty.S)) " +
                "(fct.p ?Universe.x ?Universe.y)) " +
                ":pattern ((ty ?Universe.x ty.S) (ty ?Universe.y ty.S))))",
                trans.translate(t, BOOL));
    }

    // formulas are terms in SMTLIB2
    public void test2FormTerm() throws Exception {

        SMTLib2Translator trans = new SMTLib2Translator(env);

        Term t = makeTerm("(\\forall b; id(b) -> b)");
        assertEquals("(forall ((?Bool.b Bool))" +
                " (implies (u2b (fct.id ty.bool (b2u ?Bool.b))) ?Bool.b))", trans.translate(t, BOOL));
    }

    public void testTypeArgs() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);
        Term t = makeTerm("arb as int = 4");
        assertEquals("(= (u2i (fct.arb ty.int)) 4)", trans.translate(t, BOOL));
        t = makeTerm("id(0)");
        assertEquals("(fct.id ty.int (i2u 0))", trans.translate(t, UNIVERSE));
        t = makeTerm("id(true)");
        assertEquals("(fct.id ty.bool (b2u true))", trans.translate(t, UNIVERSE));
        t = makeTerm("id(arb as 'a)");
        assertEquals("(fct.id tyvar.a (fct.arb tyvar.a))", trans.translate(t, UNIVERSE));
    }

    public void test2Ints() throws Exception {

        SMTLib2Translator trans = new SMTLib2Translator(env);

        String[] ops = { "+", "-" };
        for (String op : ops) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t, INT));
        }

        String[] preds = { "<", "<=", ">", ">=" };
        for (String op : preds) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t, BOOL));
        }

        assertEquals("(i2u 4)", trans.translate(makeTerm("4"), UNIVERSE));
        assertEquals("fct.i1", trans.translate(makeTerm("i1"), INT));
        assertTrue(trans.extrafuncs.contains("fct.i1 () Int"));
    }

    public void testEquality() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);

        assertEquals("(= fct.i2 4)", trans.translate(makeTerm("i2 = 4"), BOOL));
        assertTrue(trans.extrafuncs.contains("fct.i2 () Int"));

        assertEquals("(= (u2i (fct.id ty.int (i2u fct.i2))) 4)",
                trans.translate(makeTerm("id(i2) = 4"), BOOL));
        assertTrue(trans.extrafuncs.contains("fct.id (Type Universe) Universe"));

    }

    public void testWeakEquality() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);

        assertEquals("(= (fct.arb tyvar.a) (fct.arb tyvar.b))",
                trans.translate(makeTerm("$weq(arb as 'a, arb as 'b)"), BOOL));

        assertEquals("(= (u2i (fct.arb ty.int)) 3)",
                trans.translate(makeTerm("$weq(arb as int, 3)"), BOOL));

        assertEquals("(= (fct.arb tyvar.a) (i2u 3))",
                trans.translate(makeTerm("$weq(arb as 'a, 3)"), BOOL));
    }

    // detected a bug!
    public void testTypeQuant2() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);
        Term t = makeTerm("(\\T_all 'a; arb as 'a = arb as 'a)");

        assertEquals("(= (fct.arb tyvar.a) (fct.arb tyvar.a))", trans.translate(t.getSubterm(0), BOOL));
        assertTrue(trans.extrafuncs.contains("tyvar.a () Type"));

        assertEquals("(forall ((?Type.a Type)) (= (fct.arb ?Type.a) (fct.arb ?Type.a)))",
                trans.translate(t, BOOL));
    }

    public void testNestedTypeQuant() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);
        Term t = makeTerm("(\\T_all 'a; (\\T_all 'b; true))");
        assertEquals("(forall ((?Type.a Type) (?Type.b Type)) true)", trans.translate(t, BOOL));

        t = makeTerm("(\\T_all 'a; (\\forall x as 'a; (\\T_all 'b; (\\forall y as 'b; true))))");
        assertEquals("(forall " +
                "((?Type.a Type) (?Universe.x Universe) (?Type.b Type) (?Universe.y Universe)) " +
                "(! (implies (and (ty ?Universe.x ?Type.a) (ty ?Universe.y ?Type.b)) true) " +
                ":pattern ((ty ?Universe.x ?Type.a) (ty ?Universe.y ?Type.b))))",
                trans.translate(t, BOOL));
    }

    public void testUnknown2() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);

        assertEquals("unknown0", trans.translate(makeTerm("[0;P]true"), BOOL));
        assertEquals("unknown1", trans.translate(makeTerm("{i1:=0}i2"), BOOL));
        assertEquals("unknown2", trans.translate(makeTerm("{i1:=0}i2"), BOOL));
        assertEquals("unknown3", trans.translate(makeTerm("{i1:=0}i2"), INT));
        assertEquals("(forall ((?Int.x Int)) (> (unknown4 ?Int.x) 0))",
                trans.translate(makeTerm("(\\forall x; {i1:=0}x>0)"), BOOL));
        assertEquals("(forall ((?Type.a Type)) (> (unknown5 ?Type.a) 0))",
                trans.translate(makeTerm("(\\T_all 'a; {i1:=0}i1>0)"), BOOL));

        assertTrue(trans.extrafuncs.contains("unknown0 () Bool"));
        assertTrue(trans.extrafuncs.contains("unknown1 () Bool"));
        assertTrue(trans.extrafuncs.contains("unknown2 () Bool"));
        assertTrue(trans.extrafuncs.contains("unknown3 () Int"));
        assertTrue(trans.extrafuncs.contains("unknown4 (Int) Int"));
        assertTrue(trans.extrafuncs.contains("unknown5 (Type) Int"));
    }

    public void testUninterpreted() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);
        trans.includeTypes();

        assertEquals("(fct.arb (ty.poly ty.int (ty.poly ty.int ty.int)))",
                trans.translate(makeTerm("arb as poly(int, poly(int,int))"), UNIVERSE));
        assertTrue(trans.extrafuncs.contains("ty.poly (Type Type) Type"));

        assertEquals("(fct.f ty.int (i2u 5))", trans.translate(makeTerm("f(5) as int"), INT));
        assertEquals("(fct.arb ty.bool)", trans.translate(makeTerm("arb as bool"), UNIVERSE));
    }

    public void testCond() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);

        assertEquals("(ite fct.b1 (b2u fct.b1) (b2u fct.b2))",
                trans.translate(makeTerm("cond(b1, b1, b2)"), UNIVERSE));
        assertEquals("(ite fct.b1 fct.b1 fct.b2)",
                trans.translate(makeTerm("cond(b1, b1, b2)"), BOOL));
        assertEquals("(ite (> 5 4) 3 2)", trans.translate(makeTerm("cond(5>4, 3, 2)"), INT));
    }

    public void testPatterns() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);

        Term t = makeTerm("(\\forall x; (\\forall y; $pattern(x*y, x*y > 0)))");
        String tt = trans.translate(t, BOOL);
        assertEquals("(forall ((?Int.x Int) (?Int.y Int)) " +
                "(! (> (* ?Int.x ?Int.y) 0) :pattern ((* ?Int.x ?Int.y))))",
                tt);

        // no default pattern like above
        env = makeEnv("sort S function bool p(S,S)");
        trans = new SMTLib2Translator(env);
        t = makeTerm("(\\forall x; (\\forall y; $pattern(p(x,y), p(x,y))))");
        tt = trans.translate(t, BOOL);
        assertEquals("(forall ((?Universe.x Universe) (?Universe.y Universe)) " +
                "(! (implies (and (ty ?Universe.x ty.S) (ty ?Universe.y ty.S)) " +
                "(fct.p ?Universe.x ?Universe.y)) " +
                ":pattern ((fct.p ?Universe.x ?Universe.y))))",
                tt);
    }

    public void testTyping() throws Exception {

        env = new Environment("none:*test*", env);

        Sort sortS = new Sort("S", 0, ASTLocatedElement.CREATED);
        Type typeS = TypeApplication.getInst(sortS);
        env.addSort(sortS);

        Sort sortT = new Sort("T", 1, ASTLocatedElement.CREATED);
        env.addSort(sortT);

        env.addFunction(new Function("intResult", Environment.getIntType(),
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));

        env.addFunction(new Function("boolResult", Environment.getBoolType(),
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));

        env.addFunction(new Function("alphaResult", TypeVariable.ALPHA,
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));

        env.addFunction(new Function("betaResult", TypeVariable.BETA,
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));

        env.addFunction(new Function("s", typeS, new Type[0], false, false,
                ASTLocatedElement.CREATED));

        env.addFunction(new Function("sResult", typeS,
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));

        // t(alpha)
        TypeApplication tAlpha = TypeApplication.getInst(sortT, new Type[] {TypeVariable.ALPHA});
        env.addFunction(new Function("tResult", tAlpha,
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));

        SMTLib2Translator trans = new SMTLib2Translator(env);
        // trigger axioms for "bf"
        trans.translate(makeTerm("bf(0)"), BOOL);

        int assSize = trans.assumptions.size();
        // trigger translations
        trans.translate(makeTerm("bf(i1)"), BOOL);
        assertEquals("no assumption for i1", assSize, trans.assumptions.size());

        trans.translate(makeTerm("bf(intResult(0))"), BOOL);
        assertEquals("no assumption for intResult", assSize, trans.assumptions.size());

        trans.translate(makeTerm("bf(b1)"), BOOL);
        assertEquals("no assumption for b1", assSize, trans.assumptions.size());

        trans.translate(makeTerm("bf(s)"), BOOL);
        assertEquals("one assumption for s", ++assSize, trans.assumptions.size());
        assertEquals("Typing for function symbol fct.s\n" +
                "(ty fct.s ty.S)", trans.assumptions.getLast());

        trans.translate(makeTerm("bf(boolResult(0))"), BOOL);
        assertEquals("no assumption for boolResult", assSize, trans.assumptions.size());

        trans.translate(makeTerm("bf(alphaResult(0))"), BOOL);
        assertEquals("one assumption for alphaResult", ++assSize, trans.assumptions.size());
        assertEquals("Typing for function symbol fct.alphaResult\n" +
                "(forall ((?Type.a Type) (?x0 Universe) ) " +
                "(! (ty (fct.alphaResult ?Type.a ?x0) ?Type.a) " +
                ":pattern ((fct.alphaResult ?Type.a ?x0))))", trans.assumptions.getLast());

        trans.translate(makeTerm("bf(betaResult(0) as int)"), BOOL);
        assertEquals("one assumption for betaResult", ++assSize, trans.assumptions.size());
        assertEquals("Typing for function symbol fct.betaResult\n" +
                "(forall ((?Type.a Type) (?Type.b Type) (?x0 Universe) ) " +
                "(! (ty (fct.betaResult ?Type.a ?Type.b ?x0) ?Type.b) " +
                ":pattern ((fct.betaResult ?Type.a ?Type.b ?x0))))", trans.assumptions.getLast());

        trans.translate(makeTerm("bf(sResult(0))"), BOOL);
        assertEquals("one assumption for sResult", ++assSize, trans.assumptions.size());
        assertEquals("Typing for function symbol fct.sResult\n" +
                "(forall ((?Type.a Type) (?x0 Universe) ) " +
                "(! (ty (fct.sResult ?Type.a ?x0) ty.S) " +
                ":pattern ((fct.sResult ?Type.a ?x0))))", trans.assumptions.getLast());

        trans.translate(makeTerm("bf(tResult(0))"), BOOL);
        assertEquals("one assumption for tResult", ++assSize, trans.assumptions.size());
        assertEquals("Typing for function symbol fct.tResult\n" +
                "(forall ((?Type.a Type) (?x0 Universe) ) " +
                "(! (ty (fct.tResult ?Type.a ?x0) (ty.T ?Type.a)) " +
                ":pattern ((fct.tResult ?Type.a ?x0))))", trans.assumptions.getLast());
    }

    public void testGeneralBinders() throws Exception {
        env = makeEnv("sort S " +
                "binder S (\\B S; S)");


        SMTLib2Translator trans = new SMTLib2Translator(env);
        String t = trans.translate(makeTerm("arb = (\\B x; x)"), BOOL);
        assertEquals("(= (fct.arb ty.S) (bnd.B lambda.0 ))", t);

        try {
            assertTrue(trans.extrafuncs.contains("bnd.B ((Array Universe Universe)) Universe"));
            assertTrue(trans.extrafuncs.contains("lambda.0 () (Array Universe Universe)"));
        } catch (AssertionFailedError e) {
            System.err.println("This failed, so print the functions:");
            System.err.println(trans.extrafuncs);
            throw e;
        }
    }

    public void testPolymorphicBinders() throws Exception {
        env = makeEnv("include \"$int.p\" " +
                "sort set('a)" +
                "binder set('a) (\\setComp 'a; bool)" +
                "function bool in('a, set('a)) infix :: 50");

        SMTLib2Translator trans = new SMTLib2Translator(env);
        String t = trans.translate(makeTerm("(\\setComp x; x > 0)"), UNIVERSE);
        assertEquals("(bnd.setComp ty.int lambda.0 )", t);

        try {
            assertTrue(trans.extrafuncs.contains("bnd.setComp (Type (Array Universe Bool)) Universe"));
            assertTrue(trans.extrafuncs.contains("lambda.0 () (Array Universe Bool)"));
        } catch (AssertionFailedError e) {
            System.err.println("This failed, so print the functions:");
            System.err.println(trans.extrafuncs);
            throw e;
        }

        assertEquals("Lambda-Definition of lambda.0\n" +
                    "(forall ((?x Universe)) (let ((?Int.x (u2i ?x))) " +
                    "(= (select lambda.0 ?x) (> ?Int.x 0))))",
                    trans.assumptions.getLast());

        t = trans.translate(makeTerm("(\\forall y; y :: (\\setComp x; x > y))"), BOOL);
        assertEquals("(forall ((?Int.y Int)) (fct.in ty.int (i2u ?Int.y)" +
                " (bnd.setComp ty.int (lambda.1 ?Int.y) )))", t);

        try {
            assertTrue(trans.extrafuncs.contains("bnd.setComp (Type (Array Universe Bool)) Universe"));
            assertTrue(trans.extrafuncs.contains("lambda.1 ( Int) (Array Universe Bool)"));
        } catch (AssertionFailedError e) {
            System.err.println("This failed, so print the functions:");
            System.err.println(trans.extrafuncs);
            throw e;
        }

        assertEquals("Lambda-Definition of lambda.1\n"+
                "(forall ((?x Universe) (?Int.y Int)) (let ((?Int.x (u2i ?x))) " +
                "(= (select (lambda.1 ?Int.y) ?x) (> ?Int.x ?Int.y))))",
                trans.assumptions.getLast());
    }

    // from a bug - a very nasty one indeed!!
    public void testFreeTypeVar() throws Exception {
        SMTLib2Translator trans = new SMTLib2Translator(env);

        env = new Environment("none:*test*", env);
        env.addSort(new Sort("array", 1, ASTLocatedElement.CREATED));
        String t2 = trans.translate(makeTerm("(\\forall j as int; (\\forall v as 'ty_v; (\\T_all 'ty_v;true)))"), BOOL);
        assertEquals("(forall ((?Int.j Int) (?Universe.v Universe) (?Type.ty_v Type)) " +
                "(! (implies (and (ty ?Universe.v tyvar.ty_v)) true) " +
                ":pattern ((ty ?Universe.v tyvar.ty_v))))", t2);
        assertTrue(trans.extrafuncs.contains("tyvar.ty_v () Type"));

    }


}
