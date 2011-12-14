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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.auto.AutomationTests;
import de.uka.iti.pseudo.justify.TestRuleProblemExtractor;
import de.uka.iti.pseudo.justify.TestSchemaVariableUseVisitor;

public class AllCmdTests {
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestRuleProblemExtractor.class);
        suite.addTestSuite(TestSchemaVariableUseVisitor.class);
        suite.addTest(AutomationTests.suite());
        //$JUnit-END$
        return suite;
    }

}
