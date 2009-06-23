package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestSubtermReplacer extends TestCaseWithEnv {
    
    public void testReplace() throws Exception {
        Term org = TermMaker.makeAndTypeTerm("{ i1 := 1 } (i1+1)", env);
        Term two = TermMaker.makeAndTypeTerm("2", env);
        
        System.out.println(SubtermCollector.collect(org));
        
        Term result = SubtermReplacer.replace(org, 1, two);
        Term expected = TermMaker.makeAndTypeTerm("{ i1 := 2 } (i1+1)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 2, two);
        expected = TermMaker.makeAndTypeTerm("{ i1 := 1 } 2", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 3, two);
        expected = TermMaker.makeAndTypeTerm("{ i1 := 1 } (2+1)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 4, two);
        expected = TermMaker.makeAndTypeTerm("{ i1 := 1 } (i1+2)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 0, two);
        assertEquals(two, result);
        
        result = SubtermReplacer.replace(org, 1000, two);
        assertNull(result);
    }
    
    // was a bug!
    public void testBinding() throws Exception {
        Term org = makeTerm("(\\forall n; n > 0) -> [ 1; P]");
        Term two = TermMaker.makeAndTypeTerm("true", env);
        
        System.out.println(SubtermCollector.collect(org));
        
        Term result = SubtermReplacer.replace(org, 5, two);
        Term expected = makeTerm("(\\forall n; n > 0) -> true");
        assertEquals(expected, result);
    }
    
}
