package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.term.TestTermParser;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo.parser");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestFileParser.class);
        suite.addTestSuite(TestTermParser.class);
        //$JUnit-END$
        return suite;
    }

}
