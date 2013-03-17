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
package de.uka.iti.pseudo.prettyprint;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.prettyprint.AnnotatedString;
import de.uka.iti.pseudo.prettyprint.AnnotatedString.Style;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.term.Term;

public class TestAnnotatedString extends TestCaseWithEnv {

    public TestAnnotatedString() {
        super();
    }

    public void testOne() {
        AnnotatedString as = new AnnotatedString(0);
        as.print("0123");
        as.handleBeginTerm(1);
        as.print("456");
        as.handleBeginTerm(2);
        as.print("789");
        as.handleEndTerm();
        as.handleEndTerm();

        assertTrue(as.hasEmptyStacks());

        assertEquals("[Element[begin=4;end=10;attr=1], " +
                "Element[begin=7;end=10;attr=1.2]]", as.describeAllElements());

        assertEquals(4, as.getTermElementAt(5).getBegin());
        assertEquals(10, as.getTermElementAt(5).getEnd());
        assertEquals("Element[begin=0;end=10;attr=]", as.getTermElementAt(0).toString());
        assertEquals(new SubtermSelector(1), as.getTermElementAt(5).getSubtermSelector());
    }

    public void testHasEmptyStack() {
        AnnotatedString as = new AnnotatedString(0);
        as.print("0123");
        as.handleBeginTerm(1);
        as.print("456");
        as.handleBeginTerm(2);
        as.print("789");
        as.handleEndTerm();
        assertFalse(as.hasEmptyStacks());
        as.handleEndTerm();
        assertTrue(as.hasEmptyStacks());
    }

    // from a bug
    public void testTerm() throws Exception {
        Term t = makeTerm("b1 & b2 -> b2");
        AnnotatedString as = PrettyPrint.print(env, t);
        assertEquals("[Element[begin=0;end=7;attr=0], "+
                     "Element[begin=0;end=2;attr=0.0], " +
                     "Element[begin=5;end=7;attr=0.1], " +
                     "Element[begin=11;end=13;attr=1]]", as.describeAllElements());
    }

    // example from the javadoc
    public void testStyled() throws Exception {

        AnnotatedString as = new AnnotatedString(0);

        as.print("Hello ");
        as.handlePushStyle(Style.TYPE);
        as.print("world ");
        as.handlePushStyle(Style.KEYWORD);
        as.print("again ");
        as.handlePopStyle();
        as.print("and again");
        assertFalse(as.hasEmptyStacks());
        as.handlePopStyle();
        assertTrue(as.hasEmptyStacks());

        assertEquals("[Pair[[],Hello ], Pair[[TYPE],world ], " +
        		"Pair[[TYPE, KEYWORD],again ], Pair[[TYPE],and again]]", as.describeStyledOutput());
    }

    // from a bug
    public void testVariableTerm() throws Exception {
        Term t = makeTerm("(\\exists x; x>0)");
        PrettyPrint pp = new PrettyPrint(env, false);
        AnnotatedString as = pp.print(t);

        // trigger output.
        as.handlePushStyle(null);
        assertEquals("[Pair[[],(\\exists ], Pair[[VARIABLE],x], " +
        		"Pair[[],; ], Pair[[VARIABLE],x], Pair[[], > 0)]]", as.describeStyledOutput());

    }

}
