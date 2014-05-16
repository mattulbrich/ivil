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
import java.util.Map;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class TestCaseWithEnv provides infrastructure for test cases which need
 * environments.
 */
public abstract class TestCaseWithEnv extends TestCase {

    /**
     * DEFAULT_ENV is a default environment loaded from "testenv.p" and remains
     * unchanged. It is fixed right after construction.
     */
    protected static final Environment DEFAULT_ENV = loadDefaultEnv();

    /**
     * NO_LOCALS means no local symbols are defined.
     */
    protected static final SymbolTable NO_LOCALS = SymbolTable.EMPTY;

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

    private static final boolean ASSERTIONS = Boolean.valueOf(System.getProperty(
            "pseudo.test.assertions", "true"));

    static {
        ClassLoader.getSystemClassLoader()
                .setDefaultAssertionStatus(ASSERTIONS);
    }

    /**
     * provide the object needed for {@link #DEFAULT_ENV}.
     */
    private static Environment loadDefaultEnv() {
        try {
            Parser fp = new Parser();
            URL url = TestCaseWithEnv.class.getResource("testenv.p");
            if(url == null) {
                throw new Error("testenv.p not found!");
            }
            EnvironmentMaker em = new EnvironmentMaker(fp, url);
            Environment env = em.getEnvironment();
            env.setFixed();
            return env;
        } catch (Exception e) {
            throw new RuntimeException("Error while reading 'testenv.p'", e);
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
        return makeTerm(string, new SymbolTable(env));
    }

    /**
     * Parse a string to produce a term.
     *
     * The environment used for name resolution is {@link #env}.
     *
     * @param string
     *            the string to parse
     * @param table
     *            a local symbol table for lookup
     *
     * @return the term which was represented by the argument
     *
     * @throws Exception
     *             various things can fail during the translation.
     */
    protected Term makeTerm(String string, SymbolTable table) throws TermException {
        try {
            return TermMaker.makeAndTypeTerm(string, table, "*test*");
        } catch (Exception e) {
            throw new TermException("Cannot parse: " + string, e);
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
        return makeEnv(string, Environment.BUILT_IN_ENV);
    }


    /**
     * Parse a string to produce an environment.
     *
     * The string is prefixed by {@code include "$base.p"}.
     *
     * @param string
     *            the string to parse
     *            @param env
     *            the parent environment to set
     *
     * @return the environment which was represented by the argument
     *
     * @throws Exception
     *             various things can fail during the translation.
     */
    protected static Environment makeEnv(String string, Environment parent) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" "
                + string), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test", parent);
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
        return makeEnvAndProblems(url).fst();
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
    protected static Pair<Environment, Map<String, Sequent>>
            makeEnvAndProblems(URL url) throws Exception {
        Parser fp = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(fp, url);
        Environment env = em.getEnvironment();
        Map<String, Sequent> problems = em.getProblemSequents();
        return Pair.make(env, problems);
    }

    /**
     * Debug output. Print to stderr is only performed if {@link #VERBOSE} is
     * set to true.
     *
     * @param message
     *            message to print
     */
    public static void out(Object message) {
        if(VERBOSE) {
            if(message instanceof Throwable) {
                ((Throwable)message).printStackTrace();
            } else {
                System.err.println(message);
            }
        }
    }

}
