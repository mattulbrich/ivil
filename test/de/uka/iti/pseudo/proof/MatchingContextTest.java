package de.uka.iti.pseudo.proof;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermUnification;

public class MatchingContextTest extends TestCase {
    
    private Environment env;
    
    public MatchingContextTest() throws Exception {
        env = TestTermParser.loadEnv();
    }

    public void testMatch1() throws Exception {
        Term t1 = TermMaker.makeTerm("%a", env);
        Term t2 = TermMaker.makeTerm("2+2", env);
        TermUnification mc = new TermUnification();
        mc.leftUnify(t1, t2);
    }
    
}
