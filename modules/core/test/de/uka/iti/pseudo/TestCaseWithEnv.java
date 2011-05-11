/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo;

import java.io.StringReader;
import java.net.URL;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class TestCaseWithEnv provides infrastructure for test cases which need
 * environments.
 */
public class TestCaseWithEnv extends TestCase {

    /**
     * DEFAULT_ENV is a default environment loaded from "testenv.p" and remains
     * unchanged. It is fixed right after construction.
     */
    protected static final Environment DEFAULT_ENV = loadDefaultEnv();

    /**
     * The environment in use.
     */
    protected Environment env = DEFAULT_ENV;

    /**
     * A flag which can be set on the commandline/settings to have verbose
     * output or not
     */
    public static final boolean VERBOSE = Boolean.valueOf(System.getProperty(
            "pseudo.test.verbose", "false"));

    /**
     * provide the object needed for {@link #DEFAULT_ENV}.
     */
    private static Environment loadDefaultEnv() {
        try {
            Parser fp = new Parser();
            URL url = TestCaseWithEnv.class.getResource("testenv.p");
            if(url == null)
                throw new Error("testenv.p not found!");
            EnvironmentMaker em = new EnvironmentMaker(fp, url);
            Environment env = em.getEnvironment();
            env.setFixed();
            return env;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the environment to be used throughout the test.
     * 
     * By default it returns a reference to {@link #DEFAULT_ENV}. Any subclass
     * may decide to overwrite this.
     * 
     * This method is called during the object construction of the test. You may
     * chose to call it in setUp or anywhere else. ...
     * 
     * @return the environment to be used throughout the test.
     */
//    protected Environment getEnvironment() {
//        return DEFAULT_ENV;
//    }

    static {
        Util.registerURLHandlers();
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    /**
     * Parse a string to produce a term.
     * 
     * The environment used for name resolution is {@link #env}.
     * 
     * @param string
     *            the string to parse
     * 
     * @return the term which was represented by the argument
     * 
     * @throws Exception
     *             various things can fail during the translation.
     */
    protected Term makeTerm(String string) throws Exception {
        try {
            return TermMaker.makeAndTypeTerm(string, env, "*test*");
        } catch (Exception e) {
            System.err.println("Cannot parse: " + string);
            throw e;
        }
    }

    /**
     * Parse a string to produce an environment.
     * 
     * The string is prefixed by {@code include "$base.p"}.
     * 
     * @param string
     *            the string to parse
     * 
     * @return the environment which was represented by the argument
     * 
     * @throws Exception
     *             various things can fail during the translation.
     */
    protected static Environment makeEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" "
                + string), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        return env;
    }
    
    /**
     * Parse a URL to produce an environment.
     * 
     * @param url
     *            the url to parse
     * 
     * @return the environment which was represented by the argument
     * 
     * @throws Exception
     *             various things can fail during the translation.
     */
    protected static Environment makeEnv(URL url) throws Exception {
        Parser fp = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(fp, url);
        Environment env = em.getEnvironment();
        return env;
    }
    
    /**
     * Defbug output. Print to stderr is only performed if {@link #VERBOSE} is
     * set to true.
     * 
     * @param message
     *            message to print
     */
    public static void out(Object message) {
        if(VERBOSE)
            System.err.println(message);
    }

}
