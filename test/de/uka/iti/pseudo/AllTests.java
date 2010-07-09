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
import de.uka.iti.pseudo.auto.TestSMTLibTranslator;
import de.uka.iti.pseudo.auto.TestZ3;
import de.uka.iti.pseudo.auto.TestZ3Translator;
import de.uka.iti.pseudo.environment.TestProgramChanger;
import de.uka.iti.pseudo.environment.TestTypeVariableCollector;
import de.uka.iti.pseudo.gui.TestPrettyPrint;
import de.uka.iti.pseudo.gui.parameters.TestParameterSheet;
import de.uka.iti.pseudo.justify.TestRuleProblemExtractor;
import de.uka.iti.pseudo.parser.TestProgramParser;
import de.uka.iti.pseudo.parser.TestTermParser;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.file.TestRuleParsing;
import de.uka.iti.pseudo.proof.TestProofNode;
import de.uka.iti.pseudo.proof.TestSubtermSelector;
import de.uka.iti.pseudo.proof.TestTermSelector;
import de.uka.iti.pseudo.rule.TestGoalAction;
import de.uka.iti.pseudo.rule.TestRule;
import de.uka.iti.pseudo.rule.meta.TestLoopInvariantProgramMetaFunction;
import de.uka.iti.pseudo.rule.meta.TestMetaFunctions;
import de.uka.iti.pseudo.rule.meta.TestTermReplacer;
import de.uka.iti.pseudo.rule.meta.TestUpdSimplification;
import de.uka.iti.pseudo.rule.where.TestWhereConditions;
import de.uka.iti.pseudo.term.TestApplication;
import de.uka.iti.pseudo.term.TestProgramTerm;
import de.uka.iti.pseudo.term.TestStatements;
import de.uka.iti.pseudo.term.creation.TestSchemaCollectorVisitor;
import de.uka.iti.pseudo.term.creation.TestSubtermReplacer;
import de.uka.iti.pseudo.term.creation.TestTermInstantiator;
import de.uka.iti.pseudo.term.creation.TestTermUnification;
import de.uka.iti.pseudo.term.creation.TestTypeUnification;
import de.uka.iti.pseudo.util.TestAnnotatedString;
import de.uka.iti.pseudo.util.TestAppendMap;
import de.uka.iti.pseudo.util.TestLinearLookupMap;
import de.uka.iti.pseudo.util.TestRewindMap;
import de.uka.iti.pseudo.util.TestUtil;
import de.uka.iti.pseudo.util.settings.TestSettings;

public class AllTests {
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestFileParser.class);
        suite.addTestSuite(TestTermParser.class);
        suite.addTestSuite(TestProgramParser.class);
        suite.addTestSuite(TestProgramChanger.class);
        suite.addTestSuite(TestRuleParsing.class);
        suite.addTestSuite(TestWhereConditions.class);
        suite.addTestSuite(TestMetaFunctions.class);
        suite.addTestSuite(TestUpdSimplification.class);
        suite.addTestSuite(TestLoopInvariantProgramMetaFunction.class);
        suite.addTestSuite(TestPrettyPrint.class);
        suite.addTestSuite(TestAnnotatedString.class);
        // Deprecated: // suite.addTestSuite(TestSubtermCollector.class);
        suite.addTestSuite(TestSubtermReplacer.class);
        suite.addTestSuite(TestTermUnification.class);
        suite.addTestSuite(TestTypeUnification.class);
        suite.addTestSuite(TestApplication.class);
        suite.addTestSuite(TestStatements.class);
        suite.addTestSuite(TestProofNode.class);
        suite.addTestSuite(TestRule.class);
        suite.addTestSuite(TestGoalAction.class);
        suite.addTestSuite(TestTermSelector.class);
        suite.addTestSuite(TestSubtermSelector.class);
        suite.addTestSuite(TestProgramTerm.class);
        suite.addTestSuite(TestRuleProblemExtractor.class);
        suite.addTestSuite(TestSchemaCollectorVisitor.class);
        suite.addTestSuite(TestTypeVariableCollector.class);
        suite.addTestSuite(TestTermInstantiator.class);
        suite.addTestSuite(TestTermReplacer.class);
        suite.addTestSuite(TestUtil.class);
        suite.addTestSuite(TestAppendMap.class);
        suite.addTestSuite(TestRewindMap.class);
        suite.addTestSuite(TestSMTLibTranslator.class);
        suite.addTestSuite(TestZ3.class);
        suite.addTestSuite(TestZ3Translator.class);
        suite.addTestSuite(TestSettings.class);
        suite.addTestSuite(TestLinearLookupMap.class);
        suite.addTestSuite(TestParameterSheet.class);
        //$JUnit-END$
        return suite;
    }

}
