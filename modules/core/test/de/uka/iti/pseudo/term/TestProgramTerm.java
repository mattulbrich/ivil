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
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestProgramTerm extends TestCaseWithEnv {
    
    // due to a bug
    public void testProgramEqualities() throws Exception {
        assertEquals(makeTerm("[0;P]true"), makeTerm("[0;P]true"));
        assertFalse(makeTerm("[0;P]true").equals(makeTerm("[0;P]false")));
        assertFalse(makeTerm("[0;P]true").equals(makeTerm("[[0;P]]true")));
        assertFalse(makeTerm("[0;Q]true").equals(makeTerm("[0;P]true")));
        assertFalse(makeTerm("[1;Q]true").equals(makeTerm("[0;Q]true")));
    }
    
    public void testSchemaProgramEqualities() throws Exception {
        assertEquals(makeTerm("[%a]true"), makeTerm("[%a]true"));
        assertFalse(makeTerm("[%a]false").equals(makeTerm("[%a]true")));
        assertFalse(makeTerm("[[%a]]true").equals(makeTerm("[%a]true")));
        assertFalse(makeTerm("[%a: skip]true").equals(makeTerm("[%a]true")));
        assertFalse(makeTerm("[%a]%phi").equals(makeTerm("[%a]true")));
    }
}
