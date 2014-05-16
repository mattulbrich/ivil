/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Dump;

public class TestLoopInvariantProgramMetaFunction extends TestCaseWithEnv  {

    private Environment testEnv(String resource) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(
                new InputStreamReader(getClass().getResourceAsStream(resource)), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
//        if(VERBOSE)
//            env.dump();
        return env;
    }


    public void testCollectAssignables() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX,
                env.getProgram("P"), Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, Environment.getTrue(), null, lst);

        loopMod.collectAssignables();

        Set<Function> modifiedAssignables = loopMod.getModifiedAssignables();
        assertEquals(2, modifiedAssignables.size());
        assertTrue(modifiedAssignables.contains(env.getFunction("a")));
        assertTrue(modifiedAssignables.contains(env.getFunction("c")));
    }

    public void testVarAtPreCreation() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);
        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX,
                env.getProgram("P"), Environment.getTrue());
        LoopModifier loopMod = new LoopModifier(prog, Environment.getTrue(), makeTerm("var"), lst);
        loopMod.apply();

        assertEquals("varAtPre1 as int", loopMod.getVarAtPre().toString(true));
    }

    public void testInvariantRule() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(5, Modality.BOX,
                env.getProgram("Q"), Environment.getTrue());
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), makeTerm("var"), lst);
        loopMod.setVarAtPre(makeTerm("varAtPre"));

        LiteralProgramTerm progResult = loopMod.apply();

        assertEqualProgs(progResult.getProgram(), env.getProgram("Q_after"));
    }

    public void testInvariantRuleNonIntVariant() throws Exception {
        env = testEnv("loopTest4.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX_TERMINATION,
                env.getProgram("P"), Environment.getTrue());
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), makeTerm("var"), lst);
        loopMod.setVarAtPre(makeTerm("varAtPre"));

        LiteralProgramTerm progResult = loopMod.apply();

        assertEqualProgs(progResult.getProgram(), env.getProgram("P_after"));
    }

    public void testInvariantRuleWithoutVar() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(5, Modality.BOX,
                env.getProgram("Q"), Environment.getTrue());
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);

        LiteralProgramTerm progResult = loopMod.apply();

        assertEqualProgs(progResult.getProgram(), env.getProgram("Q_after_without_var"));
    }

    // Program with loop directly to the skip
    public void testBug1() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX,
                env.getProgram("Bug1"), Environment.getTrue());
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);

        LiteralProgramTerm progResult = loopMod.apply();

        assertEqualProgs(progResult.getProgram(), env.getProgram("Bug1_after"));
    }

    // Program with loop directly after the skip
    public void testBug2() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(1,
                Modality.BOX, env.getProgram("Bug2"), Environment.getTrue());
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);

        LiteralProgramTerm progResult = loopMod.apply();

        assertEqualProgs(progResult.getProgram(), env.getProgram("Bug2_after"));
    }

    public void testChangeAfterLoop() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX, env
                .getProgram("ChangeAfterLoop"), Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);
        loopMod.apply();

        Function fctA = env.getFunction("a");

        assertEquals(Collections.singleton(fctA), loopMod.getModifiedAssignables());
    }

    public void testParallelAssignment() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX, env
                .getProgram("ParallelAssignment"), Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);
        loopMod.apply();

        Function fctA = env.getFunction("a");
        Function fctB = env.getFunction("b");

        Set<Function> modifiedAssignables = loopMod.getModifiedAssignables();
        assertEquals(2, modifiedAssignables.size());
        assertTrue(modifiedAssignables.contains(fctA));
        assertTrue(modifiedAssignables.contains(fctB));

    }

    public void testBugTermination() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX, env
                .getProgram("Bug_termination"), Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);
        loopMod.apply();

        // just ensure that this terminates ...
        // ... was a bug when the end of the sequence was reached.
    }

    private void assertEqualProgs(Program p1, Program p2) {
        try {
            List<Statement> s1 = p1.getStatements();
            List<Statement> s2 = p2.getStatements();
            assertEquals("Lengths do not match.", s1.size(), s2.size());
            for (int i = 0; i < s1.size(); i++) {
                // System.out.println("P1: " + s1.get(i));
                // System.out.println("P2: " + s2.get(i));
                assertEquals(s1.get(i), s2.get(i));
            }
        } catch (AssertionFailedError e) {
            System.out.println("Expected:");
            Dump.dumpProgram(p1);
            System.out.println("Received:");
            Dump.dumpProgram(p2);
            throw e;
        }
    }

    public void testBugInLoopDetection() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX, env
                .getProgram("BugInLoopDetect"), Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);
        loopMod.apply();

        Function fctA = env.getFunction("a");
        assertEquals(Collections.singleton(fctA), loopMod.getModifiedAssignables());
    }

    // was a bug: b was havoced also in Q'
    public void testBugWithAssumeFalse() throws Exception {
        env = testEnv("loopTest2.p.txt");
        SymbolTable lst = new SymbolTable(env);

        {
            // Check that P' and Q are same
            Program P = env.getProgram("P");
            assertNotNull(P);

            LiteralProgramTerm prog = LiteralProgramTerm.getInst(0, Modality.BOX,
                    P, Environment.getTrue());

            LoopModifier loopMod = new LoopModifier(prog, makeTerm("true"), null, lst);
            loopMod.apply();

            Program Q = lst.getProgram("Q");
            assertNotNull(Q);
            Program Pprime = lst.getProgram("P'");
            assertNotNull(Pprime);

            assertEqualProgs(Q, Pprime);
        }
        {
            // Check that Q' and R are same
            Program Q = env.getProgram("Q");
            assertNotNull(Q);
            Program R = env.getProgram("R");
            assertNotNull(R);

            LiteralProgramTerm prog = LiteralProgramTerm.getInst(8, Modality.BOX,
                    Q, Environment.getTrue());
            LoopModifier loopMod = new LoopModifier(prog, makeTerm("true"), null, lst);
            loopMod.apply();
            assertEqualProgs(R, lst.getProgram("Q'"));
        }

    }

    public void testSplittingInvariant() throws Exception {
        env = testEnv("loopTest3.p.txt");
        SymbolTable lst = new SymbolTable(env);

        Program P = env.getProgram("P");
        assertNotNull(P);
        LiteralProgramTerm prog =
                LiteralProgramTerm.getInst(0, Modality.BOX, P, Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("(a=1&b=2)&(c=3&d=4)"), null, lst);
        loopMod.apply();

        assertEqualProgs(lst.getProgram("Q"), lst.getProgram("P'"));
    }

    public void testSplittingInvariantUpdated() throws Exception {
        env = testEnv("loopTest3.p.txt");
        SymbolTable lst = new SymbolTable(env);

        Program P2 = lst.getProgram("P2");
        assertNotNull(P2);
        LiteralProgramTerm prog =
                LiteralProgramTerm.getInst(0, Modality.BOX, P2, Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("{e:=7}((a=1&b=2)&(c=3&d=4))"), null, lst);
        loopMod.apply();

        assertEqualProgs(lst.getProgram("Q2"), lst.getProgram("P2'"));
    }

    public void testGoBeyond() throws Exception {
        env = testEnv("loopTest1.p.txt");
        SymbolTable lst = new SymbolTable(env);

        LiteralProgramTerm prog = LiteralProgramTerm.getInst(100, Modality.BOX, env
                .getProgram("GoBeyond"), Environment.getTrue());

        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, lst);
        try {
            loopMod.apply();
            fail("should raise EnvironmentException");
        } catch (EnvironmentException e) {
        }

    }
}
