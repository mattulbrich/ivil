package de.uka.iti.pseudo.gui;

import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;
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
        testTerm("(i1+i2)*i3", "(i1 + i2) * i3");
        testTerm("[ 6; P ]", "[ 6; P ]"); 
        testTerm("{b1 := true}{i1:=0}i2", "{ b1 := true }{ i1 := 0 }i2");
        testTerm("[[%a : assert %b]]", "[[ %a : assert %b ]]");
        testTerm("! !b1", "! !b1");
        testTerm("! -5 = -3", "! -5 = -3");
        testTerm("!((!b1) = true)", "!(!b1) = true");
        testTerm("!(1=1)", "!1 = 1");
        testTerm("(\\forall x; x = 5)", "(\\forall x; x = 5)");
        testTerm("(\\forall x; x > 5)", "(\\forall x; x > 5)");
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
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
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
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
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
        //                           01234567890
        AnnotatedString<Term> as = PrettyPrint.print(env, t);
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
    
    public void testOrderConincidesWithSubtermCollector() throws Exception {
        testOrderEqual("1 + 2 +3");
        testOrderEqual("1 + (2+3)");
        testOrderEqual("{ i1 := i2 + i3 }(i1 = i3)");
        testOrderEqual("[[ %a : goto %n, %k ]]");
        testOrderEqual("[ 6 ; P ]");
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
