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
        
        TermSelector ts4 = new TermSelector(ts1, 3, 4, 5);
        assertEquals("A.0.1.2.3.4.5", ts4.toString());
        
        TermSelector ts5 = new TermSelector(ts1, new SubtermSelector(3,4,5));
        assertEquals("A.0.1.2.3.4.5", ts5.toString());
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
    
    public void testSubtermConstructor() throws Exception {
        SubtermSelector s1 = new SubtermSelector(1,2,3);
        SubtermSelector s2 = new SubtermSelector();
        SubtermSelector s3 = new SubtermSelector(4);
        
        assertEquals("1.2.3", new SubtermSelector(s1, s2).toString());
        assertEquals("1.2.3.4", new SubtermSelector(s1, s3).toString());
        assertEquals("1.2.3.4.5.6", new SubtermSelector(s1, 4,5,6).toString());
        assertEquals("4.5.6", new SubtermSelector(4,5,6).toString());
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
    
    // from a bug
    public void testEquality() throws Exception {
        TermSelector t1 = new TermSelector("S.0");
        TermSelector t2 = new TermSelector("S.0.1");
        
        assertFalse(t1.equals(t2));
    }
    
    // from a bug
    public void testEquality2() throws Exception {
        TermSelector t1 = new TermSelector("A.0");
        TermSelector t2 = new TermSelector("A.1");
        
        assertFalse(t1.equals(t2));
    }
    
    public void testGetToplevel() throws Exception {
        TermSelector t1 = new TermSelector("A.1.2.3.4");
        TermSelector t2 = new TermSelector("A.1");
        assertEquals(t1.getToplevelSelector(), t2);
    }
    
    public void testPrefix() throws Exception {
        TermSelector t = new TermSelector("A.1.2.3.4");
        TermSelector t1 = new TermSelector("A.1");
        TermSelector t2 = new TermSelector("A.1.3");
        TermSelector t3 = new TermSelector("S.1.2.3.4");
        
        assertTrue(t.hasPrefix(t1));
        assertTrue(t.hasPrefix(t));
        assertFalse(t.hasPrefix(t2));
        assertFalse(t.hasPrefix(t3));
        
        assertFalse(t1.hasPrefix(t));
    }

}
