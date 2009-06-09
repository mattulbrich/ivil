/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class TestTermParser extends TestCaseWithEnv {

    private void testTerm(String term, String expected, boolean typed)
            throws Exception {
        Term t = makeTerm(term);
        assertEquals(expected, t.toString(typed));
    }

    private void testTerm(String term, boolean typed) throws Exception {
        testTerm(term, term, typed);
    }

    private void testTermFail(String term) throws Exception {
        try {
            Term t = TermMaker.makeAndTypeTerm(term, env);
            fail(term + " should not be parsable, but parses as: " + t.toString(true));
        } catch (ASTVisitException e) {
        }
    }
    
    // emerged from a bug (3)
    public void testBinder() throws Exception {
        testTerm("(\\some %x as 't;true as bool)", "(\\some %x as 't;true as bool) as 't", true); 
        testTerm("(\\forall %x; %x = 5)", "(\\forall %x as int;$eq(%x as int,5 as int) as bool) as bool", true);
        
        testTermFail("(\\forall %x as bool; %x as int > 5)"); 
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

    public void testOccurCheck() throws Exception {
        try {
            TermMaker.makeAndTypeTerm("arb as 'a = arb as set('a)", env);
            fail("should not be parsable");
        } catch (ASTVisitException e) {
        }
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
        testTerm("[i1:=1]i1", "[i1:=1]i1", false);
        testTerm("[while b1 do i1:=0 end]true", false);
        testTerm("[if b1 then skip else i1:=0 end]false", false);
        testTerm("[if b1 then skip end]b2", false);
        testTerm("[skip; skip; skip]b2", false); 
        testTermFail("[i2:=1]i2");
        testTerm("[i1:=1]i1", "[i1:=1 as int](i1 as int)", true);
        testTerm("([skip]b1) = b1", "$eq([skip]b1,b1)", false);
    }
    
//    public void testModalityPrecedence() throws Exception {
//        Term t1 = TermMaker.makeTerm("[skip]b1 as bool", env);
//        Term t2 = TermMaker.makeTerm("[skip](b1 as bool)", env);
//    }
    
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
    
}