package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.algo.TestAlgoVisitor;
import de.uka.iti.pseudo.algo.TestChainedRelationVisitor;
import de.uka.iti.pseudo.algo.TestParser;
import de.uka.iti.pseudo.algo.TestTermVisitor;

public class AllExpTests {

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
