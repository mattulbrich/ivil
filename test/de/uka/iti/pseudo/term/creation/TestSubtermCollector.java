package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Variable;

public class TestSubtermCollector extends TestCaseWithEnv {

    public void testCollect() throws Exception {

        String string = "{ i1 := 1 }{ b1 := true || i1 := { i1 := 0 }22*99 }(i1 = 3+2*1)";
        Term term = makeTerm(string);
        List<Term> subterms = SubtermCollector.collect(term);

        //System.out.println(Util.listTerms(subterms));
        
        int i = 0;
        assertEquals(term, subterms.get(i++));
        assertEquals(makeTerm("{ b1 := true || i1 := { i1 := 0 }22*99 }(i1 = 3+2*1)"), subterms.get(i++));
        assertEquals(makeTerm("i1=3+2*1"), subterms.get(i++));
        assertEquals(makeTerm("i1"), subterms.get(i++));
        assertEquals(makeTerm("3+2*1"), subterms.get(i++));
        assertEquals(makeTerm("3"), subterms.get(i++));
        assertEquals(makeTerm("2*1"), subterms.get(i++));
        assertEquals(makeTerm("2"), subterms.get(i++));
        assertEquals(makeTerm("1"), subterms.get(i++));
        assertEquals(makeTerm("true"), subterms.get(i++));
        assertEquals(makeTerm("{ i1 := 0 }22*99"), subterms.get(i++));
        assertEquals(makeTerm("{ i1 := 0 }22"), subterms.get(i++));
        assertEquals(makeTerm("22"), subterms.get(i++));
        assertEquals(makeTerm("0"), subterms.get(i++));
        assertEquals(makeTerm("99"), subterms.get(i++));
        
        
        assertEquals(makeTerm("1 as int"), subterms.get(i++));
    }
    
    public void testCollect2() throws Exception {
        String string = "{ i1 := i2 + i3 }(i1 = i3)";
        Term term = makeTerm(string);
        List<Term> subterms = SubtermCollector.collect(term);

        int i = 0;
        assertEquals(term, subterms.get(i++));
        assertEquals(makeTerm("i1=i3"), subterms.get(i++));
        assertEquals(makeTerm("i1"), subterms.get(i++));
        assertEquals(makeTerm("i3"), subterms.get(i++));
        assertEquals(makeTerm("i2 + i3"), subterms.get(i++));
        assertEquals(makeTerm("i2"), subterms.get(i++));
        assertEquals(makeTerm("i3"), subterms.get(i++));
        
    }
    
    public void testCollect3() throws Exception {
        String string = "(\\forall x; x>0)";
        Term term = makeTerm(string);
        List<Term> subterms = SubtermCollector.collect(term);
        
        int i = 0;
        assertEquals(term, subterms.get(i++));
        assertEquals(term.getSubterm(0), subterms.get(i++));
        assertEquals(new Variable("x", Environment.getIntType()), subterms.get(i++));
        assertEquals(makeTerm("0"), subterms.get(i++));
    }

}
