/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.UnificationException;

public class TestTypeUnification extends TestCaseWithEnv {

    // due to a bug
    public void testBoolInt() throws Exception {
        TypeUnification tu = new TypeUnification();
        
        try {
            tu.leftUnify(Environment.getBoolType(), Environment.getIntType());
            fail("Should fail");
        } catch (UnificationException e) {
            // should fail
        }
    }
    
}
