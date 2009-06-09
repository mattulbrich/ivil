package de.uka.iti.pseudo.term.creation;

import java.util.List;

import junit.framework.TestCase;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.Term;

public class TestSubtermCollector extends TestCaseWithEnv {

    public void testCollect() throws Exception {

        String string = "[i1:=1 ; while b1 do if b2 then skip else i1:=1+1 end end](i1 = 3+2*1)";
        Term term = makeTerm(string);
        List<Term> subterms = SubtermCollector.collect(term);

        int i = 0;
        assertEquals(term, subterms.get(i++));
        assertEquals(makeTerm("1 as int"), subterms.get(i++));
        assertEquals(makeTerm("b1 as bool"), subterms.get(i++));
        assertEquals(makeTerm("b2 as bool"), subterms.get(i++));
        assertEquals(makeTerm("1+1"), subterms.get(i++));
        assertEquals(makeTerm("1"), subterms.get(i++));
        assertEquals(makeTerm("1"), subterms.get(i++));
        assertEquals(makeTerm("i1=3+2*1"), subterms.get(i++));
        assertEquals(makeTerm("i1"), subterms.get(i++));
        assertEquals(makeTerm("3+2*1"), subterms.get(i++));
        assertEquals(makeTerm("3"), subterms.get(i++));
        assertEquals(makeTerm("2*1"), subterms.get(i++));
        assertEquals(makeTerm("2"), subterms.get(i++));
        assertEquals(makeTerm("1"), subterms.get(i++));
    }
    
    public void testCollect2() throws Exception {
        String string = "[ i1 := i2 + i3 ](i1 = i3)";
        Term term = makeTerm(string);
        List<Term> subterms = SubtermCollector.collect(term);

        int i = 0;
        assertEquals(term, subterms.get(i++));
        assertEquals(makeTerm("i2 + i3"), subterms.get(i++));
        assertEquals(makeTerm("i2"), subterms.get(i++));
        assertEquals(makeTerm("i3"), subterms.get(i++));
        assertEquals(makeTerm("i1=i3"), subterms.get(i++));
        assertEquals(makeTerm("i1"), subterms.get(i++));
        assertEquals(makeTerm("i3"), subterms.get(i++));
    }

}
