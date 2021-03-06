/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule;

import java.util.Arrays;
import java.util.Collections;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Dump;

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
            if(VERBOSE) {
                System.out.println(e);
            }
        }

        // no find but replace --> fail
        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    null, Collections.<WhereClause> emptyList(), Arrays
                            .asList(replaceAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.BUILTIN);
            Dump.dumpRule(rule);
            fail("Should fail: no find but replace");
        } catch (RuleException e) {
            if(VERBOSE) {
                System.out.println(e);
            }
        }

        // no find but remove --> fail
        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    null, Collections.<WhereClause> emptyList(), Arrays
                            .asList(removeAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.BUILTIN);
            Dump.dumpRule(rule);
            fail("Should fail: no find but remove");
        } catch (RuleException e) {
            if(VERBOSE) {
                System.out.println(e);
            }
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
            Dump.dumpRule(rule);
            fail("Should fail: no find but remove");
        } catch (RuleException e) {
            if(VERBOSE) {
                System.out.println(e);
            }
        }
    }

    // bug detected by writing the test
    public void testTyping() throws Exception {
        LocatedTerm find = new LocatedTerm(makeTerm("%a as 'a"), MatchingLocation.BOTH);
        Term replace = makeTerm("arb as int");
        GoalAction replaceAction = new GoalAction("samegoal", "", false,
                replace, Collections.<Term> emptyList(), Collections
                        .<Term> emptyList());

        try {
            Rule rule = new Rule("test", Collections.<LocatedTerm> emptyList(),
                    find, Collections.<WhereClause> emptyList(), Collections
                            .singletonList(replaceAction), Collections
                            .<String, String> emptyMap(),
                    ASTLocatedElement.CREATED);
            Dump.dumpRule(rule);
            fail("Should fail: replace has different type than find");
        } catch (RuleException e) {
            if (VERBOSE) {
                System.out.println(e);
            }
        }
    }
}
