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

import java.io.StringReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TypingContext;
import de.uka.iti.pseudo.term.creation.TypingResolver;

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
            Term t = TermMaker.makeAndTypeTerm(term, env);
            fail(term + " should not be parsable, but parses as: " + t.toString(true));
        } catch (ASTVisitException e) {
        } catch (ParseException e) {
        }
    }
    
    // emerged from a bug (3)
    public void testBinder() throws Exception {
        testTerm("(\\some %x as 't;true as bool)", "(\\some %x as 't;true as bool) as 't", true); 
        testTerm("(\\forall %x; %x = 5)", "(\\forall %x as int;$eq(%x as int,5 as int) as bool) as bool", true);
        
        testTermFail("(\\forall %x as bool; %x as int > 5)"); 
        
        // type correct?
        testTerm("(\\forall %x as int; %x=%x)", 
                "(\\forall %x as int;$eq(%x as int,%x as int) as bool) as bool", true);
    }
    
    public void testNumbers() throws Exception {
        testTerm("5", "5 as int", true);
    }

    public void testTypeInference() throws Exception {
        testTerm("arb = 5", "$eq(arb as int,5 as int) as bool", true);
        testTerm("P(true, 0)", "P(true as bool,0 as int) as poly(bool,int)", true);
        testTerm("P(arb, 0) = P(0, arb)", 
                "$eq(P(arb as int,0 as int) as poly(int,int),P(0 as int,arb as int) as poly(int,int)) as bool", true);
        testTerm("Q(P(arb, arb))", "Q(P(arb as '2,arb as '2) as poly('2,'2)) as '2",true);
    }
    
//    public void testTyvarBinder() throws Exception {
//        testTerm("(\\T_all 'a; true)", false);
//        
//        // 'a must not be bound in matrix
//        testTermFail("(\\T_all 'a; true as 'a)");
//        testTermFail("(\\T_all 'a; bf(3 as 'a))");
//        
//        Parser parser = new Parser();
//        ASTTerm ast = parser.parseTerm(new StringReader("(\\T_all 'a; bf(3 as 'a))"), "none:test");
//        TypingResolver tr = new TypingResolver(env, new TypingContext());
//        ast.visit(tr);
//        System.out.println(tr.getTypingContext());
//        
//        testTerm("$or((\\T_all 'a; bf(3 as 'a)),(\\T_all 'a; bf(true as 'a)))",
//                "$or((\\T_all 'a; bf(3 as bool) as bool) as bool,(\\T_all 'a; bf(true as 'a) as bool) as bool) as bool", true);
//        
//        testTerm("$or((\\T_all 'a; bf(arb as 'a) as bool),bf(3 as 'a) as bool) as bool",
//                 "$or((\\T_all 'a; bf(arb as 'a) as bool),bf(3 as int) as bool) as bool", true);
//    }


    public void testOccurCheck() throws Exception {
        try {
            TermMaker.makeAndTypeTerm("arb as 'a = arb as set('a)", env);
            fail("should not be parsable");
        } catch (ASTVisitException e) {
        } 
        
        testTermFail("(arb as 'a) as set('a)");
    }

    public void testAs() throws Exception {
        testTerm("arb as int", "arb as int", true);
        testTerm("P(0 as 'a, arb as 'a)", "P(0 as int,arb as int) as poly(int,int)", true);
        testTerm("arb as 'a", "arb as 'a", true);
        testTerm("arb = 2", "$eq(arb as int,2 as int) as bool", true);
    }

    public void testPrecedence() throws Exception {
        testTerm("i1+i2^i3*i4", "$plus(i1,$mult($pow(i2,i3),i4))", false);
        testTerm("i1+i2^i3*i4", "$plus(i1,$mult($pow(i2,i3),i4))", false);
        testTerm("! -i1 = i2 -> b2", "$impl($not($eq($neg(i1),i2)),b2)", false);
    }

    public void testModality() throws Exception {
        testTerm("[5;P] as bool", true);
        testTerm("[[7;P]]", false);
        testTerm("[7;P] -> [9; Q]", "$impl([7;P],[9;Q])", false);
        testTerm("[[%a]]", false);
        testTerm("[%a: end %b]", false);
        testTerm("[%a: goto %b, %c]", false);
        
        testTermFail("[6; Unknown]");
        testTermFail("[6: end true");
        testTermFail("[%a: end 1");
        testTermFail("[%a || 1:=skip");
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
    
    public void testAssociativity() throws Exception {
        testTerm("b1 -> b2 -> b1", "$impl($impl(b1,b2),b1)", false);
    }
    
    public void testSchemaVariable() throws Exception {
        testTerm("%a as 'a", true);
        testTerm("%b as bool", true);
        
        testTerm("%a as bool", true);
        testTerm("%a = %b", "$eq(%a as '%b,%b as '%b) as bool", true);
        
        testTerm("%unknown", false);
        
        testTerm("%i + 1", "$plus(%i as int,1 as int) as int", true);
        
        testTerm("(\\forall %i; %i > 5)", "(\\forall %i;$gt(%i,5))", false);
    }
    
    public void testConstVsVar() throws Exception {
        Term t = makeTerm("(\\forall i1; i1>0)");
        Term st = new SubtermSelector(0,0).selectSubterm(t);
        
        assertEquals(Variable.class, st.getClass());
        assertEquals(new Variable("i1", Environment.getIntType()), st);
    }
    
}