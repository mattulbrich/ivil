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
package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.gui.parameters.TestParameterSheet;
import de.uka.iti.pseudo.prettyprint.TestPrettyPrint;
import de.uka.iti.pseudo.util.settings.TestSettings;

public class AllGuiTests {

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestPrettyPrint.class);
        suite.addTestSuite(TestParameterSheet.class);
        suite.addTestSuite(TestSettings.class);
        //$JUnit-END$
        return suite;
    }

}
