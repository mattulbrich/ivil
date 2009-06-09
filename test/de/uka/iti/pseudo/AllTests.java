package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.gui.TestPrettyPrint;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.file.TestRuleParsing;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.rule.TestWhereCondition;
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
        suite.addTestSuite(TestWhereCondition.class);
        suite.addTestSuite(TestPrettyPrint.class);
        suite.addTestSuite(TestSubtermCollector.class);
        //$JUnit-END$
        return suite;
    }

}
