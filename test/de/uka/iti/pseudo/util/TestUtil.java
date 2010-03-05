/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import junit.framework.TestCase;

public class TestUtil extends TestCase {

    public void testJoin() throws Exception {
        
        assertEquals("a.1.c", Util.join(new Object[] { "a", 1, "c" }, "."));
        assertEquals("a.null", Util.join(new Object[] { "a", null }, "."));
    }
    
}
