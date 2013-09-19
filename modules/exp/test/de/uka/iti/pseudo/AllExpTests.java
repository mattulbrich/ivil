package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.auto.strategy.TestInstantiationStrategy;
import de.uka.iti.pseudo.rule.meta.TestRefinementModifier;

public class AllExpTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllExpTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(TestRefinementModifier.class);
        suite.addTestSuite(TestInstantiationStrategy.class);
        //$JUnit-END$
        return suite;
    }

}
