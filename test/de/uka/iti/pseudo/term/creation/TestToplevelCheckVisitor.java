package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestToplevelCheckVisitor extends TestCaseWithEnv {

    ToplevelCheckVisitor checker;
    
    @Override protected void setUp() throws Exception {
        checker = new ToplevelCheckVisitor();
    }
    
    private boolean check(Term t) {
        try {
            checker.check(t);
            return true;
        } catch (TermException e) {
            return false;
        }
    }
    
    public void testCheck() throws Exception {
        
        assertTrue(check(makeTerm("1+2+3 > 5")));
        
        // int type
        assertFalse(check(makeTerm("1+2+3")));
        
        // schema var
        assertFalse(check(makeTerm("%a as bool")));
        
        // free type var
        assertFalse(check(makeTerm("arb")));
        
    }
    
    // from a bug
    public void testTestHiddenSchema() throws Exception {
        assertFalse(check(makeTerm("(\\forall i; i > %a)")));
    }

}
