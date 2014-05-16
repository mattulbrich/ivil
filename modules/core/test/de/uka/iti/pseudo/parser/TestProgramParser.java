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
package de.uka.iti.pseudo.parser;

import java.io.StringReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.util.Dump;

public class TestProgramParser extends TestCaseWithEnv {

    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
//        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        if(VERBOSE) {
            Dump.dumpEnv(env);
        }
        return env;
    }

    private void failEnv(String string) {
        try {
            Environment env = testEnv("program P   assert 1");
            fail("Environment '" + string + "' should not load");
        } catch (Exception e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testGoodProgram() throws Exception {
        Environment env = testEnv("program P " +
        		"assume true " +
        		"assert arb " +
        		"end " +
        		"goto 2, 9999");
    }

    public void testBooleanStatements() throws Exception {
        failEnv("program P   assert 1");
        failEnv("program P   assume 1");
        failEnv("program P   end 1");

        testEnv("program P  assume arb");
    }

    // found a bug
    // annotations are now only extra arguments
    public void testAnnotatedSkips() throws Exception {
        testEnv("include \"$symbex.p\"\n" +
                "function int SOMEANNOTATIONS unique\n" +
                "program P\n" +
                "skip\n" +
                "skip SOMEANNOTATIONS, 1, 2, 3\n" +
                "skip LOOPINV, true, 2");
    }

    // from a bug : typing not checked on assignments
    public void testAssignmentTyping() throws Exception {

        testEnv("function int i assignable   program P  i := arb");

        // fail("illegal program: i is int, true is bool");
        failEnv("function int i assignable   program P   i := true");
    }

    public void testSchemaInPrograms() throws Exception {
        failEnv("program P   assume %b");
        failEnv("program P   goto 4, %b");
        failEnv("program P   skip LOOPINV, %b, 4");
    }

    public void testSkipArguments() throws Exception {
        testEnv("function int a assignable\n" +
                "program P skip skip a\n" +
                "  skip a, a\n" +
                "  skip a := a\n" +
                "  skip a a:= a\n" +
                "  skip a a:\n" +
                "  skip a, a=a\n" +
                "  skip a\n");
            // last token is "a" (parser uses getToken(2))
    }

    public void testParallelAssignment() throws Exception {
        testEnv("function int i assignable  int j assignable "+
                "program P   skip i := arb || j := 2 skip");
    }
}
