package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.file.TestRuleParsing;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.rule.TestWhereCondition;

public class AllTests {
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo.parser");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestFileParser.class);
        suite.addTestSuite(TestTermParser.class);
        suite.addTestSuite(TestRuleParsing.class);
        suite.addTestSuite(TestWhereCondition.class);
        //$JUnit-END$
        return suite;
    }

}
