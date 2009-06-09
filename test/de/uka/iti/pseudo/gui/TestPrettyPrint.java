package de.uka.iti.pseudo.gui;

import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.AnnotatedString;

public class TestPrettyPrint extends TestCaseWithEnv {

    private void testTerm(String term, String expected, boolean typed) throws Exception {
        Term t = makeTerm(term);
        assertEquals(expected, PrettyPrint.print(env, t, typed, true).toString());   
    }
    
    public void testPrettyPrint() throws Exception {
        testTerm("i1+(i2+i3)", "i1 + (i2 + i3)", false);
        testTerm("(i1+i2)+i3", "i1 + i2 + i3", false);
        testTerm("(i1+i2)*i3", "(i1 + i2) * i3", false);
        testTerm("[skip](i1+i2)", "[ skip ](i1 + i2)", false);
        testTerm("([skip]i1)+i2", "[ skip ]i1 + i2", false);
        testTerm("! !b1", "! !b1", false);
        testTerm("! -5 = -3", "! -5 = -3", false);
        testTerm("!((!b1) = true)", "!(!b1) = true", false);
        testTerm("!(1=1)", "!1 = 1", false);
        testTerm("(\\forall x; x = 5)", "(\\forall x; x = 5)", false);
        testTerm("(\\forall x; x > 5)", "(\\forall x; x > 5)", false);
        testTerm("f(1+2)", "f(1 + 2)", false);
    }
    
    public void testTyped() throws Exception {
        testTerm("1 + 2", "(1 as int + 2 as int) as int", true);
        testTerm("1", "1 as int", true);
        testTerm("-1", "(-1 as int) as int", true);
        // is this correct: ?
        testTerm("! !b1", "(!((!b1 as bool) as bool)) as bool", true);
    }
    
    public void testAnnotations1() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("i1 + i2 + i3", env);
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
        assertEquals("i1 + i2 + i3", as.toString());
        assertEquals(0, as.getBegin(3));
        assertEquals(7, as.getEnd(3));
        assertEquals(0, as.getBegin(9));
        assertEquals(12, as.getEnd(9));
        assertEquals(10, as.getBegin(10));
        assertEquals(12, as.getEnd(10));
    }

    public void testAnnotations2() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("[ i1 := i2 + i3 ](i1 = i3)", env);
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
        assertEquals("[ i1 := i2 + i3 ](i1 = i3)", as.toString());
        assertEquals(8, as.getBegin(11));
        assertEquals(15, as.getEnd(11));
        assertEquals(0, as.getBegin(2));
        assertEquals(26, as.getEnd(2));
        assertEquals(17, as.getBegin(17));
        assertEquals(26, as.getEnd(17));
        assertEquals(17, as.getBegin(21));
        assertEquals(26, as.getEnd(21));

    }
    
    public void testAnnotations3() throws Exception {
        Term t = TermMaker.makeAndTypeTerm("1 + (2 + 3)", env);
        //                           01234567890
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
        assertEquals("1 + (2 + 3)", as.toString());
        assertEquals(0, as.getBegin(0));
        assertEquals(1, as.getEnd(0));
        assertEquals(0, as.getBegin(1));
        assertEquals(11, as.getEnd(1));
        assertEquals(4, as.getBegin(4));
        assertEquals(11, as.getEnd(4));
        assertEquals(5, as.getBegin(5));
        assertEquals(6, as.getEnd(5));
        assertEquals(4, as.getBegin(7));
        assertEquals(11, as.getEnd(7));
    }
    
    public void testOrderConincidesWithSubtermCollector() throws Exception {
        testOrderEqual("1 + 2 +3");
        testOrderEqual("1 + (2+3)");
        testOrderEqual("[ i1 := i2 + i3 ](i1 = i3)");
        testOrderEqual("[i1:=1 ; while b1 do if b2 then skip else i1:=1+1 end end](i1 = 3+2*1)");
    }

    private void testOrderEqual(String string) throws Exception {
        Term t = TermMaker.makeAndTypeTerm(string, env);
        List<Term> subterms = SubtermCollector.collect(t);
        AnnotatedString<Term> astring = PrettyPrint.print(env, t);
        List<Term> annotations = astring.getAllAttributes();
        
        assertEquals(subterms.size(), annotations.size());
        for(int i = 0; i < subterms.size(); i++) {
            assertEquals(subterms.get(i), annotations.get(i));
        }
    }
    
}
