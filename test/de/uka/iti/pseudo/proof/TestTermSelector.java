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
package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestTermSelector extends TestCaseWithEnv {

    public void testTermSelectors() throws Exception {
        TermSelector ts1 = new TermSelector("A.0.1.2");
        TermSelector ts2 = new TermSelector(TermSelector.ANTECEDENT, 0, 1, 2);
        assertEquals(ts1, ts2);
        assertEquals("A.0.1.2", ts2.toString());
        assertEquals(0, ts1.getTermNo());
        assertEquals(2, ts1.getDepth());

        TermSelector ts3 = new TermSelector(ts1, 3);
        assertEquals("A.0.1.2.3", ts3.toString());
    }

    public void testStringConstructors() throws FormatException {

        try {
            new TermSelector("A.0.");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        try {
            new TermSelector("A..");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        try {
            new TermSelector("A");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        try {
            new TermSelector(".A.0");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        try {
            new TermSelector("A.-1");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        try {
            new TermSelector("T.0");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        try {
            // too large constant
            new TermSelector("A.130");
            fail("Should throw FormatException");
        } catch (FormatException e) {
        }

        new TermSelector("A.0");
        new TermSelector("A.0.0");
        new TermSelector("A.127.126");

    }

    // from a bug
    public void testNonNullConstruction() throws Exception {
        TermSelector ts = new TermSelector(TermSelector.ANTECEDENT, 33);
        assertEquals(33, ts.getTermNo());
    }

    // from a bug
    public void testIsTopLevel() throws Exception {
        TermSelector t1 = new TermSelector("S.0");
        TermSelector t2 = new TermSelector("S.0.1");

        assertTrue(t1.isToplevel());
        assertFalse(t2.isToplevel());
    }

}
