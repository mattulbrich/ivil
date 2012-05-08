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

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

public class TestSMTLibTranslator extends TestCaseWithEnv {
    
    private static SMTLib1Translator.ExpressionType FORMULA1 = SMTLib1Translator.ExpressionType.FORMULA;
    private static SMTLib1Translator.ExpressionType INT1 = SMTLib1Translator.ExpressionType.INT;
    private static SMTLib1Translator.ExpressionType UNIVERSE1 = SMTLib1Translator.ExpressionType.UNIVERSE;
    
    public void test1Quantifiers() throws Exception {
        
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        Term t = makeTerm("(\\forall x; x > 0)");
        assertEquals("(forall (?Int.x Int) (> ?Int.x 0))",
                trans.translate(t, FORMULA1));
        
        t = makeTerm("(\\forall x as 'a; x = x)");
        assertEquals("(forall (?Universe.x Universe) (implies (= (ty ?Universe.x) tyvar.a) (= ?Universe.x ?Universe.x)))",
                trans.translate(t, FORMULA1));
        
        t = makeTerm("(\\exists y; 0 <= y)");
        assertEquals("(exists (?Int.y Int) (<= 0 ?Int.y))",
                trans.translate(t, FORMULA1));
        
        t = makeTerm("(\\exists x as 'a; x = x)");
        assertEquals("(exists (?Universe.x Universe) (and (= (ty ?Universe.x) tyvar.a) (= ?Universe.x ?Universe.x)))",
                trans.translate(t, FORMULA1));
    }
    
    public void test1NestedQuantifier() throws Exception {
        
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        Term t = makeTerm("(\\forall x; (\\forall y; x > y))");
        assertEquals("(forall (?Int.x Int) (?Int.y Int) (> ?Int.x ?Int.y))",
                trans.translate(t, FORMULA1));
        
        t = makeTerm("(\\exists x; (\\exists y; x > y))");
        assertEquals("(exists (?Int.x Int) (?Int.y Int) (> ?Int.x ?Int.y))",
                trans.translate(t, FORMULA1));
        
        t = makeTerm("(\\exists x; (\\forall y; x > y))");
        assertEquals("(exists (?Int.x Int) (forall (?Int.y Int) (> ?Int.x ?Int.y)))",
                trans.translate(t, FORMULA1));
        
        env = makeEnv("sort S function bool p(S,S)");
        
        // Universe quantifications
        t = makeTerm("(\\forall x; (\\forall y; p(x,y)))");
        assertEquals("(forall (?Universe.x Universe) (?Universe.y Universe) " +
                "(implies (= (ty ?Universe.x) ty.S) (implies (= (ty ?Universe.y) ty.S) (= (fct.p ?Universe.x ?Universe.y) termTrue))))",
                trans.translate(t, FORMULA1));
        
        t = makeTerm("(\\exists x; (\\exists y; p(x,y)))");
        assertEquals("(exists (?Universe.x Universe) (?Universe.y Universe) " +
                "(and (= (ty ?Universe.x) ty.S) (and (= (ty ?Universe.y) ty.S) (= (fct.p ?Universe.x ?Universe.y) termTrue))))",
                trans.translate(t, FORMULA1));
    }
    
    // formulas and terms are different in SMTLIB1
    public void test1FormTerm() throws Exception {
        
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        Term t = makeTerm("(\\forall b; id(b) -> b)");
        assertEquals("(forall (?Universe.b Universe) (implies (= (ty ?Universe.b) ty.bool)" +
                " (implies (= (fct.id ?Universe.b) termTrue) (= ?Universe.b termTrue))))", trans.translate(t, FORMULA1));
    }
    
    public void test1CollectSchema() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
        Term t = makeTerm("arb as int = 4");
        assertEquals("(= (fct.arb ty.int) (i2u 4))", trans.translate(t, FORMULA1));
    }
    
    public void testInts() throws Exception {
        
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        String[] ops = { "+", "-" };
        for (String op : ops) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t, INT1));
        }
        
        String[] preds = { "<", "<=", ">", ">=" };
        for (String op : preds) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t, FORMULA1));
        }
        
        assertEquals("(i2u 4)", trans.translate(makeTerm("4"), UNIVERSE1));
        assertEquals("fct.i1", trans.translate(makeTerm("i1"), INT1));
        assertTrue(trans.extrafuncs.contains("(fct.i1  Int)"));
        
        // TODO How about: equality on integers should be on integers not on universe?! (MU)
        assertEquals("(= (i2u fct.i2) (i2u 4))", trans.translate(makeTerm("i2 = 4"), FORMULA1));
        assertTrue(trans.extrafuncs.contains("(fct.i2  Int)"));
        
        assertEquals("(= (fct.id (i2u fct.i2)) (i2u 4))", trans.translate(makeTerm("id(i2) = 4"), FORMULA1));
        assertTrue(trans.extrafuncs.contains("(fct.id Universe Universe)"));
        
    }
    
    public void testTypeQuant() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
        Term t = makeTerm("(\\T_all 'a; arb as 'a = arb as 'a)");
        
        assertEquals("(= (fct.arb tyvar.a) (fct.arb tyvar.a))", trans.translate(t.getSubterm(0), FORMULA1));
        assertTrue(trans.extrafuncs.contains("(tyvar.a Type)"));        
        
        assertEquals("(forall (?Type.a Type) (= (fct.arb ?Type.a) (fct.arb ?Type.a)))", trans.translate(t, FORMULA1));

        
    }
    
    public void testUnknown() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        assertEquals("unknown0", trans.translate(makeTerm("[0;P]true"), FORMULA1));
        assertEquals("unknown1", trans.translate(makeTerm("{i1:=0}i2"), FORMULA1));
        assertEquals("unknown2", trans.translate(makeTerm("{i1:=0}i2"), UNIVERSE1));
        assertEquals("unknown3", trans.translate(makeTerm("{i1:=0}i2"), INT1));
        assertEquals("(forall (?Int.x Int) (> (unknown4 ?Int.x) 0))", 
                trans.translate(makeTerm("(\\forall x; {i1:=0}x>0)"), FORMULA1));
        
        assertTrue(trans.extrapreds.contains("(unknown0)"));
        assertTrue(trans.extrapreds.contains("(unknown1)"));
        assertTrue(trans.extrafuncs.contains("(unknown2 Universe)"));
        assertTrue(trans.extrafuncs.contains("(unknown3 Int)"));
        assertTrue(trans.extrafuncs.contains("(unknown4 Int Int)"));
    }
    
    public void testUninterpreted() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
        trans.includeTypes();
        
        assertEquals("(fct.arb (ty.poly ty.int (ty.poly ty.int ty.int)))",
                trans.translate(makeTerm("arb as poly(int, poly(int,int))"), UNIVERSE1));
        assertTrue(trans.extrafuncs.contains("(ty.poly Type Type Type)"));
        
        assertEquals("(fct.f (i2u 5))", trans.translate(makeTerm("f(5) as int"), INT1));
        assertEquals("(fct.arb ty.bool)", trans.translate(makeTerm("arb as bool"), UNIVERSE1));
        
    }
    
    public void testCond() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
    
        assertEquals("(ite (= fct.b1 termTrue) fct.b1 fct.b2)",
                trans.translate(makeTerm("cond(b1, b1, b2)"), UNIVERSE1));
        assertEquals("(ite (= fct.b1 termTrue) (= fct.b1 termTrue) (= fct.b2 termTrue))",
                trans.translate(makeTerm("cond(b1, b1, b2)"), FORMULA1));
        assertEquals("(ite (> 5 4) 3 2)", trans.translate(makeTerm("cond(5>4, 3, 2)"), INT1));
    }
    
    public void testPatterns() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        assertEquals("(forall (?Int.x Int) (> (* ?Int.x ?Int.x) 0) :pat { (* ?Int.x ?Int.x) })",
                trans.translate(makeTerm("(\\forall x; $pattern(x*x, x*x > 0))"), FORMULA1));
    }
    
    public void testTyping() throws Exception {
        
        env = new Environment("none:*test*", env);

        env.addFunction(new Function("intResult", Environment.getIntType(),
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));
        
        env.addFunction(new Function("univResult", Environment.getBoolType(),
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));
        
        env.addFunction(new Function("alphaResult", TypeVariable.ALPHA,
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));
        
        env.addFunction(new Function("freeResult", TypeVariable.BETA,
                new Type[] { TypeVariable.ALPHA }, false, false,
                ASTLocatedElement.CREATED));
        
        SMTLib1Translator trans = new SMTLib1Translator(env);
        // trigger axioms for "bf"
        trans.translate(makeTerm("bf(0)"), FORMULA1);
        
        int assumptionsBefore = trans.assumptions.size();
        // trigger translations
        trans.translate(makeTerm("bf(i1)"), FORMULA1);
        assertEquals("no assumption for i1", assumptionsBefore, trans.assumptions.size());
        
        trans.translate(makeTerm("bf(intResult(0))"), FORMULA1);
        assertEquals("no assumption for intResult", assumptionsBefore, trans.assumptions.size());
        
        trans.translate(makeTerm("bf(b1)"), FORMULA1);
        assertEquals("Typing for function symbol fct.b1\n" +
        		"(= (ty fct.b1) ty.bool)", trans.assumptions.getLast());
        
        trans.translate(makeTerm("bf(univResult(0))"), FORMULA1);
        assertEquals("Typing for function symbol fct.univResult\n" +
                        "(forall (?Type.a Type) (?x0 Universe) (= (ty (fct.univResult ?x0)) ty.bool))", trans.assumptions.getLast());
        
        trans.translate(makeTerm("bf(alphaResult(0))"), FORMULA1);
        assertEquals("Typing for function symbol fct.alphaResult\n" +
                "(forall (?Type.a Type) (?x0 Universe) (implies (and (= (ty ?x0) ?Type.a))" +
                " (= (ty (fct.alphaResult ?x0)) ?Type.a)))", trans.assumptions.getLast());

        trans.translate(makeTerm("bf(freeResult(0) as int)"), FORMULA1);
        assertEquals("Typing for function symbol fct.freeResult\n" +
                "(forall (?Type.b Type) (?Type.a Type) (?x0 Universe) (implies (and (= (ty ?x0) ?Type.a))" +
                " (= (ty (fct.freeResult ?Type.b ?x0)) ?Type.b)))", trans.assumptions.getLast());
    }
    
    // from a bug - a very nasty one indeed!!
    public void testFreeTypeVar() throws Exception {
        SMTLib1Translator trans = new SMTLib1Translator(env);
        
        env = new Environment("none:*test*", env);
        env.addSort(new Sort("array", 1, ASTLocatedElement.CREATED));
        String t2 = trans.translate(makeTerm("(\\forall j as int; (\\forall v as 'ty_v; (\\T_all 'ty_v;true)))"), FORMULA1);
        assertEquals("(forall (?Int.j Int) (?Universe.v Universe) (implies (= (ty ?Universe.v) tyvar.ty_v) (forall (?Type.ty_v Type) true)))", t2);
        assertTrue(trans.extrafuncs.contains("(tyvar.ty_v Type)"));
        
    }
    
}
