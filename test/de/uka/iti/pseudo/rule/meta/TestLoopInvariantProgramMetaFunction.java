package de.uka.iti.pseudo.rule.meta;

import java.io.InputStreamReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.LiteralProgramTerm;

public class TestLoopInvariantProgramMetaFunction extends TestCaseWithEnv  {

    private Environment testEnv(String resource) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new InputStreamReader(getClass().getResourceAsStream(resource)), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "test");
        Environment env = em.getEnvironment();
        if(VERBOSE)
            env.dump();
        return env;
    }
    
    
    public void testCollectAssignables() throws Exception {
        
        env = testEnv("loopTest1.p.txt");
        
        LiteralProgramTerm prog = new LiteralProgramTerm(0, false, env.getProgram("P"));
        
        LoopModifier loopMod = new LoopModifier(prog, Environment.getTrue(), makeTerm("0"), env);
        
        loopMod.collectAssignables();
        
        assertEquals(2, loopMod.modifiedAssignables.size());
        assertTrue(loopMod.modifiedAssignables.contains(env.getFunction("a")));
        assertTrue(loopMod.modifiedAssignables.contains(env.getFunction("c")));
    }
    
    
}
