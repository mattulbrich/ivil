package de.uka.iti.pseudo.term.creation;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.Term;

public class SubtermReplacerTest extends TestCase {
    
    private Environment env;

    public SubtermReplacerTest() throws Exception {
        env = TestTermParser.loadEnv();
    }
    
    public void testReplace() throws Exception {
        Term org = TermMaker.makeTerm("[ i1 := 1 ] (i1+1)", env);
        Term two = TermMaker.makeTerm("2", env);
        
        System.out.println(SubtermCollector.collect(org));
        
        Term result = SubtermReplacer.replace(org, 1, two);
        Term expected = TermMaker.makeTerm("[ i1 := 2 ] (i1+1)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 2, two);
        expected = TermMaker.makeTerm("[ i1 := 1 ] 2", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 3, two);
        expected = TermMaker.makeTerm("[ i1 := 1 ] (2+1)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 4, two);
        expected = TermMaker.makeTerm("[ i1 := 1 ] (i1+2)", env);
        assertEquals(expected, result);
        
        result = SubtermReplacer.replace(org, 0, two);
        assertEquals(two, result);
    }
    
}
