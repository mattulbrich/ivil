package de.uka.iti.pseudo.rule;

import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestGoalAction extends TestCaseWithEnv {

    public void testConstructor() throws Exception {
        
        try {
            new GoalAction("newgoal", "", true, null, Collections
                    .<Term> emptyList(), Collections.<Term> emptyList());
            fail("Should fail: newgoal and remove");
        } catch (RuleException e) {
            // supposed to fail
        }
        
        
        try {
            new GoalAction("newgoal", "", false, makeTerm("true") , Collections
                    .<Term> emptyList(), Collections.<Term> emptyList());
            fail("Should fail: newgoal and replace");
        } catch (RuleException e) {
            // supposed to fail
        }
        
        try {
            new GoalAction("samegoal", "", true, makeTerm("true") , Collections
                    .<Term> emptyList(), Collections.<Term> emptyList());
            fail("Should fail: remove and replace");
        } catch (RuleException e) {
            // supposed to fail
        }

        
    }

}
