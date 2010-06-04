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
package de.uka.iti.pseudo.justify;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;

public class TestRuleProblemExtractor extends TestCaseWithEnv {

    private static Environment loadEnv() {
        try {
            Parser fp = new Parser(TestRuleProblemExtractor.class.getResourceAsStream("extractor.p"));
            ASTFile f = fp.File();
            f.setFilename("*test_internal*");
            de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
            EnvironmentMaker em = new EnvironmentMaker(fp, f, "none:*test_internal*");
            Environment environment = em.getEnvironment();
            environment.setFixed();
            return environment;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private Environment startEnv;
    
    public TestRuleProblemExtractor() {
        startEnv = loadEnv(); 
    }
    
    public void test_extract_both() throws Exception {
        testRule("extract_both");
    }
    
    public void test_extract_left() throws Exception {
        testRule("extract_left");
    }
    
    public void test_extract_right() throws Exception {
        testRule("extract_right");
    }
    
    public void test_extract_findless() throws Exception {
        testRule("extract_findless");
    }
    
    public void test_extract_findless_assume_less() throws Exception {
        testRule("extract_findless_assume_less");
    }
    
    public void test_rename_schemas() throws Exception {
        testRule("rename_schemas");
    }

    public void test_rename_schemas2() throws Exception {
        testRule("rename_schemas2");
    }

    public void test_rename_schema3() throws Exception {
        testRule("rename_schema3");
    }

    public void test_rename_schema4() throws Exception {
        testRule("rename_schema4");
    }

    public void test_rename_schema5() throws Exception {
        testRule("rename_schema5");
    }

    public void test_skolemize() throws Exception {
        testRule("skolemize");
    }
                    
    private void testRule(String name) throws Exception {

        Rule rule = startEnv.getRule(name);
        if(rule == null)
            fail("Unknown rule " + name);

        String expected = rule.getProperty("expectedTranslation");
        if(expected == null)
            fail("Rule " + name + " has no expectedTranslation");

        env = new Environment("none:wrap", startEnv);
        RuleProblemExtractor rpe = new RuleProblemExtractor(rule, env);
        Term result = rpe.extractProblem();

        Term expectedTerm;
        try {
            expectedTerm = makeTerm(expected);
        } catch(Exception ex) {
            env.dump();
            System.out.println(result);
            throw ex;
        }

        if(!result.equals(expectedTerm)) {
            env.dump();
            rule.dump();

            System.out.println(expectedTerm);
            System.out.println(result);

            PrettyPrint pp = new PrettyPrint(env);
            System.out.println(pp.print(expectedTerm));
            System.out.println(pp.print(result));

            pp.setTyped(true);               
            System.out.println(pp.print(expectedTerm));
            System.out.println(pp.print(result));
        }

        assertEquals(expectedTerm, result);
    }
}

