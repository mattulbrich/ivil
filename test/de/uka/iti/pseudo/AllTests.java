package de.uka.iti.pseudo;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.gui.TestPrettyPrint;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.file.TestRuleParsing;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.rule.meta.TestMetaFunctions;
import de.uka.iti.pseudo.rule.meta.TestTermReplacer;
import de.uka.iti.pseudo.rule.where.TestWhereConditions;
import de.uka.iti.pseudo.term.TestApplication;
import de.uka.iti.pseudo.term.TestProgramTerm;
import de.uka.iti.pseudo.term.creation.TestSchemaCollectorVisitor;
import de.uka.iti.pseudo.term.creation.TestSubtermCollector;
import de.uka.iti.pseudo.term.creation.TestSubtermReplacer;
import de.uka.iti.pseudo.term.creation.TestTermInstantiator;
import de.uka.iti.pseudo.term.creation.TestTermUnification;
import de.uka.iti.pseudo.term.creation.TestTypeUnification;
import de.uka.iti.pseudo.util.TestAnnotatedString;

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
        suite.addTestSuite(TestWhereConditions.class);
        suite.addTestSuite(TestMetaFunctions.class);
        suite.addTestSuite(TestPrettyPrint.class);
        suite.addTestSuite(TestAnnotatedString.class);
        suite.addTestSuite(TestSubtermCollector.class);
        suite.addTestSuite(TestSubtermReplacer.class);
        suite.addTestSuite(TestTermUnification.class);
        suite.addTestSuite(TestTypeUnification.class);
        suite.addTestSuite(TestApplication.class);
        suite.addTestSuite(TestProgramTerm.class);
        suite.addTestSuite(TestSchemaCollectorVisitor.class);
        suite.addTestSuite(TestTermInstantiator.class);
        suite.addTestSuite(TestTermReplacer.class);
        //$JUnit-END$
        return suite;
    }

}
