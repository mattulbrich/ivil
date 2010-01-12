package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.GotoStatement;

public class TestStatements extends TestCaseWithEnv {

    // due to a bug
    public void testGotoConstructor() throws Exception {
        try {
            GotoStatement g = new GotoStatement(2, new Term[] { makeTerm("i1") });
            System.out.println(g);
            fail("Should have failed: Invalid argument to constructor");
        } catch(TermException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
        
        try {
            GotoStatement g = new GotoStatement(2, new Term[] { makeTerm("%x as bool") });
            System.out.println(g);
            fail("Should have failed: Invalid argument to constructor");
        } catch(TermException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
        
        try {
            GotoStatement g = new GotoStatement(2, new Term[0]);
            System.out.println(g);
            fail("Should have failed: Invalid argument to constructor");
        } catch(TermException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
    }
    
    public void testAssignment() throws Exception {
        try {
            AssignmentStatement a = new AssignmentStatement(makeTerm("i1"), makeTerm("true"));
            System.out.println(a);
            fail("Should have failed: Invalid argument to constructor");
        } catch(TermException ex) {
            if(VERBOSE)
                ex.printStackTrace();
        }
    }
}
