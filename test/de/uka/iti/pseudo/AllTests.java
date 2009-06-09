package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.gui.TestPrettyPrint;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.file.TestRuleParsing;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.proof.TestTermUnification;
import de.uka.iti.pseudo.rule.where.TestNewSkolem;
import de.uka.iti.pseudo.rule.where.TestSubst;
import de.uka.iti.pseudo.term.creation.SubtermReplacerTest;
import de.uka.iti.pseudo.term.creation.TestSubtermCollector;

public class AllTests {
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestFileParser.class);
        suite.addTestSuite(TestTermParser.class);
        suite.addTestSuite(TestRuleParsing.class);
        suite.addTestSuite(TestNewSkolem.class);
        suite.addTestSuite(TestSubst.class);
        suite.addTestSuite(TestPrettyPrint.class);
        suite.addTestSuite(TestSubtermCollector.class);
        suite.addTestSuite(SubtermReplacerTest.class);
        suite.addTestSuite(TestTermUnification.class);
        //$JUnit-END$
        return suite;
    }

}
