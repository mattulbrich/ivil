/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.parser.term;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class TestTermParser extends TestCase {
    
    private static final String ENV_FILE = "test/de/uka/iti/pseudo/parser/term/parsertest.p";
    
    private Environment env;
    
    private static Environment loadEnv() throws FileNotFoundException, ParseException, ASTVisitException {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        FileParser fp = new FileParser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File(ENV_FILE));
        return em.getEnvironment();
    }
    
    public TestTermParser() throws FileNotFoundException, ParseException, ASTVisitException  {
        env = loadEnv();
        System.out.println("Environment: ");
        env.dump();
    }
    
    private void testTerm(String term, String expected, boolean typed) 
            throws Exception { 
        Term t = TermMaker.makeTerm(term, env);
        assertEquals(expected, t.toString(typed));
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
            TermMaker.makeTerm("arb as 'a = arb as set('a)", env);
            fail("should not be parsable");
        } catch (ASTVisitException e) {
        }
    }
    
    public void testAs() throws Exception {
        testTerm("arb as int", "arb as int", true);
        testTerm("P(0 as 'a, arb as 'a)", "P(0 as int,arb as int) as poly(int,int)", true);
        testTerm("arb as 'a", "arb as 'a", true);
    }
    
    public void testPrecedence() throws Exception {
        testTerm("i1+i2^i3*i4", "$plus(i1,$mult($pow(i2,i3),i4))", false);
        testTerm("i1+i2^i3*i4", "$plus(i1,$mult($pow(i2,i3),i4))", false);
        testTerm("! -i1 = i2 -> b2", "$impl($not($eq($neg(i1),i2)),b2)", false);
    }
    
    public void testModality() throws Exception {
        testTerm("[i1:=1]i1", "[i1:=1]i1", false);
    }

}