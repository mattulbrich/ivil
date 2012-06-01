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
import de.uka.iti.pseudo.auto.TestSMTLib2Translator;
import de.uka.iti.pseudo.auto.TestSMTLibTranslator;
import de.uka.iti.pseudo.auto.TestZ3;
import de.uka.iti.pseudo.auto.TestZ3Translator;
import de.uka.iti.pseudo.auto.strategy.hint.TestHintParser;
import de.uka.iti.pseudo.auto.strategy.hint.TestHintStrategy;
import de.uka.iti.pseudo.auto.strategy.hint.TestHints;
import de.uka.iti.pseudo.environment.TestProgramChanger;
import de.uka.iti.pseudo.environment.TestRuleAxiomExtractor;
import de.uka.iti.pseudo.environment.TestTypeVariableCollector;
import de.uka.iti.pseudo.parser.TestProgramParser;
import de.uka.iti.pseudo.parser.TestTermParser;
import de.uka.iti.pseudo.parser.file.TestFileParser;
import de.uka.iti.pseudo.parser.file.TestRuleParsing;
import de.uka.iti.pseudo.prettyprint.TestTermTag;
import de.uka.iti.pseudo.proof.TestProofNode;
import de.uka.iti.pseudo.proof.TestRuleApplicationMaker;
import de.uka.iti.pseudo.proof.TestSubtermSelector;
import de.uka.iti.pseudo.proof.TestTermSelector;
import de.uka.iti.pseudo.proof.serialisation.TestValidXSD;
import de.uka.iti.pseudo.proof.serialisation.TestXMLOutput;
import de.uka.iti.pseudo.rule.TestGoalAction;
import de.uka.iti.pseudo.rule.TestRule;
import de.uka.iti.pseudo.rule.meta.TestLoopInvariantProgramMetaFunction;
import de.uka.iti.pseudo.rule.meta.TestMetaFunctions;
import de.uka.iti.pseudo.rule.meta.TestTermReplacer;
import de.uka.iti.pseudo.rule.meta.TestUpdSimplification;
import de.uka.iti.pseudo.rule.where.TestWhereConditions;
import de.uka.iti.pseudo.term.TestApplication;
import de.uka.iti.pseudo.term.TestMapTypes;
import de.uka.iti.pseudo.term.TestProgramTerm;
import de.uka.iti.pseudo.term.TestStatements;
import de.uka.iti.pseudo.term.TestTermComparator;
import de.uka.iti.pseudo.term.TestTypeVariableBinding;
import de.uka.iti.pseudo.term.TestUpdates;
import de.uka.iti.pseudo.term.creation.TestSchemaCollectorVisitor;
import de.uka.iti.pseudo.term.creation.TestSubtermReplacer;
import de.uka.iti.pseudo.term.creation.TestTermInstantiator;
import de.uka.iti.pseudo.term.creation.TestTermUnification;
import de.uka.iti.pseudo.term.creation.TestToplevelCheckVisitor;
import de.uka.iti.pseudo.term.creation.TestTypeUnification;
import de.uka.iti.pseudo.util.TestAnnotatedString;
import de.uka.iti.pseudo.util.TestConcurrentSoftHashCache;
import de.uka.iti.pseudo.util.TestLinearLookupMap;
import de.uka.iti.pseudo.util.TestRewindMap;
import de.uka.iti.pseudo.util.TestTextInstantiator;
import de.uka.iti.pseudo.util.TestUtil;

public class AllCoreTests {

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo - core");

        //$JUnit-BEGIN$
        suite.addTestSuite(TestValidXSD.class);
        suite.addTestSuite(TestFileParser.class);
        suite.addTestSuite(TestRuleAxiomExtractor.class);
        suite.addTestSuite(TestTermParser.class);
        suite.addTestSuite(TestTermTag.class);
        suite.addTestSuite(TestProgramParser.class);
        suite.addTestSuite(TestProgramChanger.class);
        suite.addTestSuite(TestRuleParsing.class);
        suite.addTestSuite(TestRuleApplicationMaker.class);
        suite.addTestSuite(TestWhereConditions.class);
        suite.addTestSuite(TestMetaFunctions.class);
        suite.addTestSuite(TestUpdates.class);
        suite.addTestSuite(TestUpdSimplification.class);
        suite.addTestSuite(TestLoopInvariantProgramMetaFunction.class);
        suite.addTestSuite(TestAnnotatedString.class);
        //suite.addTestSuite(TestResetHashMap.class);
        // Deprecated: // suite.addTestSuite(TestSubtermCollector.class);
        suite.addTestSuite(TestSubtermReplacer.class);
        suite.addTestSuite(TestTermUnification.class);
        suite.addTestSuite(TestTypeUnification.class);
        suite.addTestSuite(TestApplication.class);
        suite.addTestSuite(TestTypeVariableBinding.class);
        suite.addTestSuite(TestStatements.class);
        suite.addTestSuite(TestProofNode.class);
        suite.addTestSuite(TestRule.class);
        suite.addTestSuite(TestGoalAction.class);
        suite.addTestSuite(TestTermSelector.class);
        suite.addTestSuite(TestSubtermSelector.class);
        suite.addTestSuite(TestProgramTerm.class);
        suite.addTestSuite(TestSchemaCollectorVisitor.class);
        suite.addTestSuite(TestTypeVariableCollector.class);
        suite.addTestSuite(TestTermInstantiator.class);
        suite.addTestSuite(TestTermComparator.class);
        suite.addTestSuite(TestTextInstantiator.class);
        suite.addTestSuite(TestTermReplacer.class);
        suite.addTestSuite(TestUtil.class);
        //suite.addTestSuite(TestAppendMap.class);
        suite.addTestSuite(TestRewindMap.class);
        suite.addTestSuite(TestHintParser.class);
        suite.addTestSuite(TestHintStrategy.class);
        suite.addTestSuite(TestHints.class);
        suite.addTestSuite(TestSMTLibTranslator.class);
        suite.addTestSuite(TestSMTLib2Translator.class);
        suite.addTestSuite(TestZ3.class);
        suite.addTestSuite(TestZ3Translator.class);
        suite.addTestSuite(TestLinearLookupMap.class);
        suite.addTestSuite(TestToplevelCheckVisitor.class);
        suite.addTestSuite(TestXMLOutput.class);
        suite.addTestSuite(TestConcurrentSoftHashCache.class);
        suite.addTestSuite(TestMapTypes.class);
        //$JUnit-END$
        return suite;
    }

}
