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

import static de.uka.iti.pseudo.auto.SMTLibTranslator.ExpressionType.FORMULA;
import static de.uka.iti.pseudo.auto.SMTLibTranslator.ExpressionType.INT;
import static de.uka.iti.pseudo.auto.SMTLibTranslator.ExpressionType.UNIVERSE;

import java.util.Arrays;
import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

public class TestSMTLibTranslator extends TestCaseWithEnv {

    public void testQuantifiers() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        Term t = makeTerm("(\\forall x; x > 0)");
        assertEquals("(forall (?Int.x Int) (> ?Int.x 0))",
                trans.translate(t, FORMULA));
        
        t = makeTerm("(\\forall x as 'a; x = x)");
        assertEquals("(forall (?Universe.x Universe) (implies (= (ty ?Universe.x) tyvar.a) (= ?Universe.x ?Universe.x)))",
                trans.translate(t, FORMULA));
        
        t = makeTerm("(\\exists y; 0 <= y)");
        assertEquals("(exists (?Int.y Int) (<= 0 ?Int.y))",
                trans.translate(t, FORMULA));
        
        t = makeTerm("(\\exists x as 'a; x = x)");
        assertEquals("(exists (?Universe.x Universe) (and (= (ty ?Universe.x) tyvar.a) (= ?Universe.x ?Universe.x)))",
                trans.translate(t, FORMULA));
    }
    
    // formulas and terms are different in SMTLIB
    public void testFormTerm() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        Term t = makeTerm("(\\forall b; id(b) -> b)");
        assertEquals("(forall (?Universe.b Universe) (implies (= (ty ?Universe.b) ty.bool)" +
        		" (implies (= (fct.id ?Universe.b) termTrue) (= ?Universe.b termTrue))))", trans.translate(t, FORMULA));
    }
    
    public void testCollectSchema() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        Term t = makeTerm("arb = 4");
        assertEquals("(= (fct.arb ty.int) (i2u 4))", trans.translate(t, FORMULA));
    }
    
    public void testInts() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        String[] ops = { "+", "-" };
        for (String op : ops) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t, INT));
        }
        
        String[] preds = { "<", "<=", ">", ">=" };
        for (String op : preds) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t, FORMULA));
        }
        
        assertEquals("(i2u 4)", trans.translate(makeTerm("4"), UNIVERSE));
        assertEquals("fct.i1", trans.translate(makeTerm("i1"), INT));
        assertTrue(trans.extrafuncs.contains("(fct.i1  Int)"));
        
        assertEquals("(= (i2u fct.i2) (i2u 4))", trans.translate(makeTerm("i2 = 4"), FORMULA));
        assertTrue(trans.extrafuncs.contains("(fct.i2  Int)"));
        
        assertEquals("(= (fct.id (i2u fct.i2)) (i2u 4))", trans.translate(makeTerm("id(i2) = 4"), FORMULA));
        assertTrue(trans.extrafuncs.contains("(fct.id Universe Universe)"));
        
    }
    
    public void testTypeQuant() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        Term t = makeTerm("(\\T_all 'a; arb as 'a = arb)");
        
        assertEquals("(= (fct.arb tyvar.a) (fct.arb tyvar.a))", trans.translate(t.getSubterm(0), FORMULA));
        assertTrue(trans.extrafuncs.contains("(tyvar.a Type)"));        
        
        assertEquals("(forall (?Type.a Type) (= (fct.arb ?Type.a) (fct.arb ?Type.a)))", trans.translate(t, FORMULA));

        
    }
    
    public void testUnknown() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        assertEquals("unknown0", trans.translate(makeTerm("[0;P]"), FORMULA));
        assertEquals("unknown1", trans.translate(makeTerm("{i1:=0}i2"), FORMULA));
        assertEquals("unknown2", trans.translate(makeTerm("{i1:=0}i2"), UNIVERSE));
        assertEquals("unknown3", trans.translate(makeTerm("{i1:=0}i2"), INT));
        assertEquals("(forall (?Int.x Int) (> (unknown4 ?Int.x) 0))", trans.translate(makeTerm("(\\forall x; {i1:=0}x>0)"), FORMULA));
        
        assertTrue(trans.extrapreds.contains("(unknown0)"));
        assertTrue(trans.extrapreds.contains("(unknown1)"));
        assertTrue(trans.extrafuncs.contains("(unknown2 Universe)"));
        assertTrue(trans.extrafuncs.contains("(unknown3 Int)"));
        assertTrue(trans.extrafuncs.contains("(unknown4 Int Int)"));
    }
    
    public void testUninterpreted() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        trans.includeTypes();
        
        assertEquals("(fct.arb (ty.poly ty.int (ty.poly ty.int ty.int)))",
                trans.translate(makeTerm("arb as poly(int, poly(int,int))"), UNIVERSE));
        assertTrue(trans.extrafuncs.contains("(ty.poly Type Type Type)"));
        
        assertEquals("(fct.f (i2u 5))", trans.translate(makeTerm("f(5) as int"), INT));
        assertEquals("(fct.arb ty.bool)", trans.translate(makeTerm("arb as bool"), UNIVERSE));
        
    }
    
    public void testCond() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
    
        assertEquals("(ite (= fct.b1 termTrue) fct.b1 fct.b2)",
                trans.translate(makeTerm("cond(b1, b1, b2)"), UNIVERSE));
        assertEquals("(ite (= fct.b1 termTrue) (= fct.b1 termTrue) (= fct.b2 termTrue))",
                trans.translate(makeTerm("cond(b1, b1, b2)"), FORMULA));
        assertEquals("(ite (> 5 4) 3 2)", trans.translate(makeTerm("cond(5>4, 3, 2)"), INT));
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
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        // trigger axioms for "bf"
        trans.translate(makeTerm("bf(0)"), FORMULA);
        
        int assumptionsBefore = trans.assumptions.size();
        // trigger translations
        trans.translate(makeTerm("bf(i1)"), FORMULA);
        assertEquals("no assumption for i1", assumptionsBefore, trans.assumptions.size());
        
        trans.translate(makeTerm("bf(intResult(0))"), FORMULA);
        assertEquals("no assumption for intResult", assumptionsBefore, trans.assumptions.size());
        
        trans.translate(makeTerm("bf(b1)"), FORMULA);
        assertEquals("Typing for function symbol fct.b1\n" +
        		"(= (ty fct.b1) ty.bool)", trans.assumptions.getLast());
        
        trans.translate(makeTerm("bf(univResult(0))"), FORMULA);
        assertEquals("Typing for function symbol fct.univResult\n" +
                        "(forall (?Type.a Type) (?x0 Universe) (= (ty (fct.univResult ?x0)) ty.bool))", trans.assumptions.getLast());
        
        trans.translate(makeTerm("bf(alphaResult(0))"), FORMULA);
        assertEquals("Typing for function symbol fct.alphaResult\n" +
                "(forall (?Type.a Type) (?x0 Universe) (implies (and (= (ty ?x0) ?Type.a))" +
                " (= (ty (fct.alphaResult ?x0)) ?Type.a)))", trans.assumptions.getLast());

        trans.translate(makeTerm("bf(freeResult(0) as int)"), FORMULA);
        assertEquals("Typing for function symbol fct.freeResult\n" +
                "(forall (?Type.b Type) (?Type.a Type) (?x0 Universe) (implies (and (= (ty ?x0) ?Type.a))" +
                " (= (ty (fct.freeResult ?Type.b ?x0)) ?Type.b)))", trans.assumptions.getLast());
    }
    
    // from a bug - a very nasty one indeed!!
    public void testFreeTypeVar() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        env = new Environment("none:*test*", env);
        env.addSort(new Sort("array", 1, ASTLocatedElement.CREATED));
        String t2 = trans.translate(makeTerm("(\\forall j as int; (\\forall v as 'ty_v; (\\T_all 'ty_v;true)))"), FORMULA);
        assertEquals("(forall (?Int.j Int) (forall (?Universe.v Universe) (implies (= (ty ?Universe.v) tyvar.ty_v) (forall (?Type.ty_v Type) true))))", t2);
        assertTrue(trans.extrafuncs.contains("(tyvar.ty_v Type)"));
        
    }
    
}
