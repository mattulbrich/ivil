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

package de.uka.iti.pseudo.parser;

import java.net.URL;
import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.ProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.statement.Statement;

public class TestTermParser extends TestCaseWithEnv {

    private void testTerm(String term, String expected, boolean typed)
            throws Exception {
        Term t = makeTerm(term);
        String actual = t.toString(typed);
        assertEquals(expected, actual);
    }

    private void testTerm(String term, boolean typed) throws Exception {
        testTerm(term, term, typed);
    }

    private void testTermFail(String term) throws Exception {
        try {
            Term t = TermMaker.makeAndTypeTerm(term, new SymbolTable(env));
            fail(term + " should not be parsable, but parses as: " + t.toString(true));
        } catch (ASTVisitException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    // emerged from a bug (3)
    public void testBinder() throws Exception {
        testTerm("(\\some %x as 't;true as bool)", "(\\some %x as 't;true as bool) as 't", true);
        testTerm("(\\some %x;true as bool)", "(\\some %x as %'x;true as bool) as %'x", true);
        testTerm("(\\forall %x; %x = 5)", "(\\forall %x as int;$eq(%x as int,5 as int) as bool) as bool", true);

        testTermFail("(\\forall %x as bool; %x as int > 5)");

        // type correct?
        testTerm("(\\forall %x as int; %x=%x)",
                "(\\forall %x as int;$eq(%x as int,%x as int) as bool) as bool", true);
    }

    // was a bug
    public void testReusedBinderVar() throws Exception {
        testTerm("(\\forall x as int; (\\forall x as bool; true) & x=x)",
                 "(\\forall x as int;$and((\\forall x as bool;true as bool) as bool," +
                 "$eq(\\var x as int,\\var x as int) as bool) as bool) as bool", true);
    }

    public void testMultiBinder() throws Exception {
        testTerm("(\\forall x, y; x>0 & y)",
                 "(\\forall x as int;(\\forall y as bool;$and($gt(\\var x as int," +
                 "0 as int) as bool,\\var y as bool) as bool) as bool) as bool", true);
    }

    public void testMultiBinderTwice() throws Exception {
        testTerm("(\\forall x as int, x as bool; x)",
                "(\\forall x as int;(\\forall x as bool;\\var x as bool) as bool) as bool", true);

        testTermFail("(\\forall x as bool, x as int; x)");
    }

    public void testExplicitVariable() throws Exception {
        testTerm("(\\exists x as int; \\var x = x)","(\\exists x;$eq(\\var x,\\var x))", false);

        Term t1 = makeTerm("\\var x as int");
        Term t2 = Variable.getInst("x", Environment.getIntType());
        assertEquals(t2, t1);

        // different x have different types
        testTerm("$and($eq(\\var x,4),$eq(\\var x,true))", false);
    }

    public void testNumbers() throws Exception {
        testTerm("5", "5 as int", true);
    }

    public void testTypeInference() throws Exception {
        testTerm("arb = 5", "$eq(arb as int,5 as int) as bool", true);
        testTerm("P(true, 0)", "P(true as bool,0 as int) as poly(bool,int)", true);
        testTerm("P(arb, 0) = P(0, arb)",
                "$eq(P(arb as int,0 as int) as poly(int,int),P(0 as int,arb as int) as poly(int,int)) as bool", true);
        testTerm("Q(P(arb, arb as 'a))", "Q(P(arb as 'a,arb as 'a) as poly('a,'a)) as 'a",true);
    }

    public void testTyvarBinder() throws Exception {

        testTerm("(\\T_all 'a;true)", false);

        testTerm("(\\T_all 'b; (arb as 'b) = (arb as 'b))",
                 "(\\T_all 'b;$eq(arb as 'b,arb as 'b) as bool) as bool", true);

        testTerm("(\\T_all 'a;(\\forall x as 'a; Q(P(x,x)) = x))",
                 "(\\T_all 'a;(\\forall x as 'a;" +
                   "$eq(Q(P(\\var x as 'a,\\var x as 'a) as poly('a,'a)) as 'a,\\var x as 'a) " +
                     "as bool) as bool) as bool", true);

        testTermFail("(\\T_ex %'a; arb as %'a = 3)");

    }

    // was a bug
    public void testExplicitTypes() throws Exception {
        testTerm("$eq(arb as 'a,arb as 'a) as bool", true);
        testTerm("arb as 'a = arb", "$eq(arb as 'a,arb as 'a) as bool", true);
        testTerm("arb as %'a = arb", "$eq(arb as %'a,arb as %'a) as bool", true);
        testTerm("(\\T_all 'a;$eq(arb as 'a,arb as 'a) as bool) as bool", true);
    }

    public void testExplicitTypesInference() throws Exception {
        Term t = makeTerm("arb as %'a");
        assertEquals(SchemaType.getInst("a"), t.getType());
    }


    public void testOccurCheck() throws Exception {
        try {
            TermMaker.makeAndTypeTerm("arb as 'a = arb as set('a)", new SymbolTable(env));
            fail("should not be parsable");
        } catch (ASTVisitException e) {
        }

        testTermFail("(arb as 'a) as set('a)");
    }

    public void testAs() throws Exception {
        testTerm("arb as int", "arb as int", true);
        testTerm("P(0 as %'a, arb as %'a)", "P(0 as int,arb as int) as poly(int,int)", true);
        testTerm("arb as %'a", "arb as %'a", true);
        testTerm("arb as 'a", "arb as 'a", true);
        testTerm("arb = 2", "$eq(arb as int,2 as int) as bool", true);
        testTermFail("0 as bool");
        testTermFail("0 as 'a");
    }

    public void testPrecedence() throws Exception {
        testTerm("i1+i2^i3*i4", "$plus(i1,$mult($pow(i2,i3),i4))", false);
        testTerm("i1+i2^i3*i4", "$plus(i1,$mult($pow(i2,i3),i4))", false);
        testTerm("! -i1 = i2 -> b2", "$impl($not($eq($neg(i1),i2)),b2)", false);
    }

    public void testModality() throws Exception {
        testTerm("[5;P](true as bool) as bool", true);
        testTerm("[5;P](%a as bool) as bool", true);
        testTerm("[[7;P]]true", false);
        testTerm("[[7;P] ]true", "[[7;P]]true", false);
        testTerm("[<7;P>]true", false);
        testTerm("[?%a?]true", false);
        testTerm("[7;P]b1 -> [9; Q]true", "$impl([7;P]b1,[9;Q]true)", false);
        testTerm("[[%a]]%b", false);
        testTerm("[%a: end]%b", false);
        testTerm("[%a: goto %b, %c]false", false);

        testTermFail("[6; Unknown]");
        testTermFail("[6: end true");
        testTermFail("[%a: end 1");
        testTermFail("[%a || 1:=skip");
        testTermFail("[0;P]]true");
        testTermFail("[?0;P?]true");
        testTermFail("[<0;P]true");
    }

    public void testAnyModality() throws Exception {

        testTerm("[?%a?]%b", false);
        testTerm("[?%a: assert %c?]%b", false);

        testTermFail("[? 0;P ?]true");
    }

    public void testUpdate() throws Exception {
        testTerm("{ i1 := 1 as int }(i1 as int)", true);
        testTerm("{ i1 := 1 || i1 := %v }%b", false);
        testTerm("{ %x := %v || %y := %t }true", false);
        testTerm("{ i1 := 1 }{ i1 := 2 }false", false);
        testTerm("{ i1 := { b1 := true }i1 }true", false);
        testTerm("{ U }{ V }true", false);
        // type inference
        testTerm("{ i1 := arb } i1", "{ i1 := arb as int }(i1 as int)", true);

        // must be assignable
        testTermFail("{ i2 := 0 }true");
        // wrong types
        testTermFail("{ b1 := 0 }true");

    }

    // was a bug
    public void testEmptyUpdate() throws Exception {
        testTerm("{  }true", false);
    }

    public void testAssociativity() throws Exception {
        testTerm("b1 -> b2 -> b1", "$impl($impl(b1,b2),b1)", false);
    }

    public void testSchemaVariable() throws Exception {
        testTerm("%a as 'a", true);
        testTerm("%b as bool", true);

        testTerm("%a as bool", true);
        testTerm("%a = %b", "$eq(%a as %'b,%b as %'b) as bool", true);

        testTerm("%unknown", false);

        testTerm("%i + 1", "$plus(%i as int,1 as int) as int", true);

        testTerm("(\\forall %i; %i > 5)", "(\\forall %i;$gt(%i,5))", false);
    }

    public void testConstVsVar() throws Exception {
        Term t = makeTerm("(\\forall i1; i1>0)");
        Term st = new SubtermSelector(0,0).selectSubterm(t);

        assertEquals(Variable.class, st.getClass());
        assertEquals(Variable.getInst("i1", Environment.getIntType()), st);
    }

    public void testMakeAndType() throws Exception {

        try {
            Term t = TermMaker.makeAndTypeTerm("3", new SymbolTable(env), "none:test", TypeVariable.ALPHA);
            assertEquals(TypeVariable.ALPHA, t.getType());
        } catch(ASTVisitException ex) {
            if(VERBOSE) {
                ex.printStackTrace();
            }
        }
    }

    // revealed a bug
    public void testLocalSymbols() throws Exception {
        Type intType = Environment.getIntType();
        Function locF = new Function("local_f", intType,
                new Type[0], false, true, ASTLocatedElement.CREATED);
        Binder locB = new Binder("\\local_b", intType,
                intType,
                new Type[] { intType },
                ASTLocatedElement.CREATED);
        Sort locS = new Sort("local_s", 0, ASTLocatedElement.CREATED);
        Program locP = new Program("local_p",
                new URL("none://tmp"),
                Collections.<Statement>emptyList(),
                Collections.<String>emptyList(),
                ASTLocatedElement.CREATED);

        SymbolTable local = new SymbolTable(env);
        local.addBinder(locB);
        local.addProgram(locP);
        local.addFunction(locF);
        local.addSort(locS);

        Term t = makeTerm("i1 + local_f", local);
        assertEquals(Application.getInst(locF, intType),
                t.getSubterm(1));

        t = makeTerm("(\\local_b x; i1)", local);
        assertEquals(Binding.getInst(locB, intType, Variable.getInst("x", intType),
                new Term[] { makeTerm("i1") }),
                t);

        t = makeTerm("\\var x as local_s", local);
        assertEquals(TypeApplication.getInst(locS), t.getType());

        t = makeTerm("[0; local_p]true", local);
        assertEquals(locP, ((LiteralProgramTerm)t).getProgram());
    }

}