package de.uka.iti.pseudo.rule;

import java.util.Arrays;
import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.term.Term;

public class TestRule extends TestCaseWithEnv {

    /*
     * rule test
     *   find true
     *   samegoal remove
     */
    public void testConstructor() throws Exception {
        
        LocatedTerm find = new LocatedTerm(makeTerm("true"), MatchingLocation.BOTH);
        GoalAction removeAction = new GoalAction("samegoal", "", true, null, Collections.<Term>emptyList(), Collections.<Term>emptyList());
        GoalAction replaceAction = new GoalAction("samegoal", "", false, makeTerm("true"), Collections.<Term>emptyList(), Collections.<Term>emptyList());
        
        // remove in unlocated find --> fail
        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    find, Collections.<WhereClause> emptyList(), Arrays
                            .asList(removeAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.BUILTIN);
            fail("Should fail: remove in unlocated find");
        } catch (RuleException e) {
            if(VERBOSE)
                System.out.println(e);
        }
        
        // no find but replace --> fail
        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    null, Collections.<WhereClause> emptyList(), Arrays
                            .asList(replaceAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.BUILTIN);
            rule.dump();
            fail("Should fail: no find but replace");
        } catch (RuleException e) {
            if(VERBOSE)
                System.out.println(e);
        }
        
        // no find but remove --> fail
        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    null, Collections.<WhereClause> emptyList(), Arrays
                            .asList(removeAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.BUILTIN);
            rule.dump();
            fail("Should fail: no find but remove");
        } catch (RuleException e) {
            if(VERBOSE)
                System.out.println(e);
        }

    }

    
    public void test2() throws Exception {
        
        LocatedTerm find = new LocatedTerm(makeTerm("true"), MatchingLocation.BOTH);
        GoalAction removeAction = new GoalAction("samegoal", "", true, null, Collections.<Term>emptyList(), Collections.<Term>emptyList());
        GoalAction replaceAction = new GoalAction("samegoal", "", false, makeTerm("true"), Collections.<Term>emptyList(), Collections.<Term>emptyList());
        
        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    find, Collections.<WhereClause> emptyList(), Arrays
                            .asList(replaceAction, removeAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.BUILTIN);
            rule.dump();
            fail("Should fail: no find but remove");
        } catch (RuleException e) {
            if(VERBOSE)
                System.out.println(e);
        }
    }
}
