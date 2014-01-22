/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.algo.TestAlgoVisitor;
import de.uka.iti.pseudo.algo.TestChainedRelationVisitor;
import de.uka.iti.pseudo.algo.TestParser;
import de.uka.iti.pseudo.algo.TestTermVisitor;

public class AllAlgoTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllExpTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(TestAlgoVisitor.class);
        suite.addTestSuite(TestChainedRelationVisitor.class);
        suite.addTestSuite(TestParser.class);
        suite.addTestSuite(TestTermVisitor.class);
        //$JUnit-END$
        return suite;
    }

}
