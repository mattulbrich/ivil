/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestUpdates extends TestCaseWithEnv {

    public void testCreation() throws Exception {
        assertEquals(makeTerm("{i1 := 3}3"), makeTerm("{i1 := 3}3"));
    }
    
}
