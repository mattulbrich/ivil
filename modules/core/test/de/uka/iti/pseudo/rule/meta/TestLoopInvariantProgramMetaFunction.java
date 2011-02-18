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
package de.uka.iti.pseudo.rule.meta;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.statement.Statement;

public class TestLoopInvariantProgramMetaFunction extends TestCaseWithEnv  {

    private Environment testEnv(String resource) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new InputStreamReader(getClass().getResourceAsStream(resource)), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
//        if(VERBOSE)
//            env.dump();
        return env;
    }
    
    
    public void testCollectAssignables() throws Exception {
        env = testEnv("loopTest1.p.txt");
        
        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env.getProgram("P"));
        
        LoopModifier loopMod = new LoopModifier(prog, Environment.getTrue(), null, env);
        
        loopMod.collectAssignables();
        
        Set<Function> modifiedAssignables = loopMod.getModifiedAssignables();
        assertEquals(2, modifiedAssignables.size());
        assertTrue(modifiedAssignables.contains(env.getFunction("a")));
        assertTrue(modifiedAssignables.contains(env.getFunction("c")));
    }
    
    public void testVarAtPreCreation() throws Exception {
        env = testEnv("loopTest1.p.txt");
        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env.getProgram("P"));
        LoopModifier loopMod = new LoopModifier(prog, Environment.getTrue(), makeTerm("var"), env);
        loopMod.apply();
        
        assertEquals("varAtPre1 as int", loopMod.getVarAtPre().toString(true));
    }
    
    public void testInvariantRule() throws Exception {
        env = testEnv("loopTest1.p.txt");
        
        LiteralProgramTerm prog = new LiteralProgramTerm(5, false, env.getProgram("Q"));
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), makeTerm("var"), env);
        loopMod.setVarAtPre(makeTerm("varAtPre"));
        
        LiteralProgramTerm progResult = loopMod.apply();
        
        assertEqualProgs(progResult.getProgram(), env.getProgram("Q_after"));
    }
    
    public void testInvariantRuleWithoutVar() throws Exception {
        env = testEnv("loopTest1.p.txt");
        
        LiteralProgramTerm prog = new LiteralProgramTerm(5, false, env.getProgram("Q"));
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
        
        LiteralProgramTerm progResult = loopMod.apply();
        
        assertEqualProgs(progResult.getProgram(), env.getProgram("Q_after_without_var"));
    }
    
    // Program with loop directly to the skip
    public void testBug1() throws Exception {
        env = testEnv("loopTest1.p.txt");
        
        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env.getProgram("Bug1"));
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
        
        LiteralProgramTerm progResult = loopMod.apply();
        
        assertEqualProgs(progResult.getProgram(), env.getProgram("Bug1_after"));
    }
    
    public void testChangeAfterLoop() throws Exception {
        env = testEnv("loopTest1.p.txt");

        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env
                .getProgram("ChangeAfterLoop"));
        
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
        loopMod.apply();
        
        Function fctA = env.getFunction("a");
        
        assertEquals(Collections.singleton(fctA), loopMod.getModifiedAssignables());
    }

    public void testParallelAssignment() throws Exception {
        env = testEnv("loopTest1.p.txt");
        
        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env
                .getProgram("ParallelAssignment"));
        
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
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

        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env
                .getProgram("Bug_termination"));
        
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
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
            p1.dump();
            p2.dump();
            throw e;
        }
    }
    
    public void testBugInLoopDetection() throws Exception {
        env = testEnv("loopTest1.p.txt");
        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env
                .getProgram("BugInLoopDetect"));
        
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
        loopMod.apply();
        
        Function fctA = env.getFunction("a");
        assertEquals(Collections.singleton(fctA), loopMod.getModifiedAssignables());
    }
    
    public void testGoBeyond() throws Exception {
        
        env = testEnv("loopTest1.p.txt");
        LiteralProgramTerm prog = new LiteralProgramTerm(100, false, env
                .getProgram("GoBeyond"));
        
        LoopModifier loopMod = new LoopModifier(prog, makeTerm("inv"), null, env);
        try {
            loopMod.apply();
            fail("should raise EnvironmentException");
        } catch (EnvironmentException e) {
        }
        
    }
}
