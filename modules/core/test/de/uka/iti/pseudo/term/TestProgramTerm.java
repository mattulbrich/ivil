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
        assertEquals(makeTerm("[0;P]"), makeTerm("[0;P]"));
        assertFalse(makeTerm("[0;P]").equals(makeTerm("[[0;P]]")));
        assertFalse(makeTerm("[0;Q]").equals(makeTerm("[0;P]")));
    }
    
}
