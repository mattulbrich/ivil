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
package de.uka.iti.pseudo.gui;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.prettyprint.TermTag;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.AnnotatedString;

public class TestPrettyPrint extends TestCaseWithEnv {
    
    PrettyPrint pp = new PrettyPrint(env);

    private void testTerm(String term, String expected) throws Exception {
        Term t = makeTerm(term);
        
        assertEquals(expected, pp.print(t).toString());   
    }
    
    public void testPrettyPrint() throws Exception {
        pp.setTyped(false);
        testTerm("i1+(i2+i3)", "i1 + (i2 + i3)");
        testTerm("(i1+i2)+i3", "i1 + i2 + i3");
        testTerm("i1+i2+i3", "i1 + i2 + i3");
        // this found a bug:
        testTerm("(i1+i2)*i3", "(i1 + i2) * i3"); 
        testTerm("[ 6; P ]true", "[ 6; P ]true"); 
        testTerm("{b1 := true}{i1:=0}i2", "{ b1 := true }{ i1 := 0 }i2");
        testTerm("[[%a : assert %b]]%phi", "[[ %a : assert %b ]]%phi");
        testTerm("! !b1", "! !b1");
        testTerm("! -5 = -3", "! -5 = -3");
        testTerm("!((!b1) = true)", "!(!b1) = true");
        testTerm("!(1=1)", "!1 = 1");
        testTerm("(\\forall x; x = 5)", "(\\forall x; x = 5)");
        testTerm("(\\forall x; x > 5)", "(\\forall x; x > 5)");
        testTerm("((\\T_all 'a; (true)))", "(\\T_all 'a; true)");
        testTerm("f(1+2)", "f(1 + 2)");
    }
    
    public void testTyped() throws Exception {
        pp.setTyped(true);
        testTerm("1 + 2", "(1 as int + 2 as int) as int");
        testTerm("1", "1 as int");
        testTerm("-1", "(-1 as int) as int");
        // is this correct: ?
        testTerm("! !b1", "(!((!b1 as bool) as bool)) as bool");
    }
    
    public void testAnnotations1() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("i1 + i2 + i3", env);
        AnnotatedString<TermTag> as = PrettyPrint.print(env, t);
        assertEquals("i1 + i2 + i3", as.toString());
        assertEquals(0, as.getBeginAt(3));
        assertEquals(7, as.getEndAt(3));
        assertEquals(0, as.getBeginAt(9));
        assertEquals(12, as.getEndAt(9));
        assertEquals(10, as.getBeginAt(10));
        assertEquals(12, as.getEndAt(10));
    }

    public void testAnnotations2() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("{ i1 := i2 + i3 }(i1 = i3)", env);
        AnnotatedString<TermTag> as = PrettyPrint.print(env, t);
        assertEquals("{ i1 := i2 + i3 }(i1 = i3)", as.toString());
        assertEquals(8, as.getBeginAt(11));
        assertEquals(15, as.getEndAt(11));
        assertEquals(0, as.getBeginAt(2));
        assertEquals(26, as.getEndAt(2));
        assertEquals(17, as.getBeginAt(17));
        assertEquals(26, as.getEndAt(17));
        assertEquals(17, as.getBeginAt(21));
        assertEquals(26, as.getEndAt(21));

    }
    
    public void testAnnotations3() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("1 + (2 + 3)", env);
        // 01234567890
        AnnotatedString<TermTag> as = PrettyPrint.print(env, t);
        assertEquals("1 + (2 + 3)", as.toString());
        assertEquals(0, as.getBeginAt(0));
        assertEquals(1, as.getEndAt(0));
        assertEquals(0, as.getBeginAt(1));
        assertEquals(11, as.getEndAt(1));
        assertEquals(4, as.getBeginAt(4));
        assertEquals(11, as.getEndAt(4));
        assertEquals(5, as.getBeginAt(5));
        assertEquals(6, as.getEndAt(5));
        assertEquals(4, as.getBeginAt(7));
        assertEquals(11, as.getEndAt(7));
    }
    
    private void testOrderEqual(String string) throws Exception {
        Term t = TermMaker.makeAndTypeTerm(string, env);
        AnnotatedString<TermTag> astring = PrettyPrint.print(env, t);
        List<TermTag> annotations = astring.getAllAttributes();
        
        SubtermCollector sc = new SubtermCollector();
        t.visit(sc);
        
        assertEquals(sc.subtermsInOrder.size(), annotations.size());
        for (TermTag termTag : annotations) {
            int pos = termTag.getTotalPos();
            assertEquals(sc.subtermsInOrder.get(pos), termTag.getTerm());
        }
    }
    
    public void testTyvarBinding() throws Exception {
        
    }
    
    private class SubtermCollector extends DefaultTermVisitor.DepthTermVisitor {
        private List<Term> subtermsInOrder = new ArrayList<Term>();

        protected void defaultVisitTerm(Term term) throws TermException {
            subtermsInOrder.add(term);
            super.defaultVisitTerm(term);
        }
    }
    
    // this needs to be the last test!
    public void testPPPlugin() throws Exception {
        env.getPluginManager().register(PrettyPrint.SERVICE_NAME, "de.uka.iti.pseudo.gui.MockPrettyPrintPlugin");
        pp = new PrettyPrint(env);
        
        testTerm("f(3)", "{-3-}");
        testTerm("g(3,4)", "g[4, 3]");
        testTerm("(\\forall x; x >0)", "ALL x ; x > 0");
        // No update rewriting at the moment, may come back later
        // testTerm("{i1:=0}i2", "{ i1 <-- 0 }i2");
        
    }
    
    // these need to be the last test!
    public void testNameReplacing() throws Exception {
        
        env = new Environment("none:generated", env);
        env.getPluginManager().register(PrettyPrint.SERVICE_NAME, "de.uka.iti.pseudo.gui.MockPrettyPrintPlugin");
        env.addFunction(new Function("Xtest", Environment.getBoolType(),
                new Type[0], false, false, ASTLocatedElement.CREATED));
        
        pp = new PrettyPrint(env);
        
        testTerm("Xtest", "test");
        testTerm("(\\forall Xx; Xx >0)", "ALL x ; x > 0");
    }
    
    // these need to be the last test!
    public void testOrderConincidesWithSubtermCollector() throws Exception {
        env.getPluginManager().register(PrettyPrint.SERVICE_NAME, "de.uka.iti.pseudo.gui.MockPrettyPrintPlugin");
        pp = new PrettyPrint(env);
        testOrderEqual("1 + 2 +3");
        testOrderEqual("1 + (2+3)");
        testOrderEqual("{ i1 := i2 + i3 }(i1 = i3)");
        testOrderEqual("[[ %a : goto %n, %k ]]%b");
        testOrderEqual("[ 6 ; P ]b1");
        testOrderEqual("f(g(3,4))");
    }

}
