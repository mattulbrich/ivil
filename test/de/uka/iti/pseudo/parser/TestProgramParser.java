package de.uka.iti.pseudo.parser;

import java.io.StringReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.file.ASTFile;

public class TestProgramParser extends TestCaseWithEnv {
    
    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "test");
        Environment env = em.getEnvironment();
        if(VERBOSE)
            env.dump();
        return env;
    }
    
    public void testGoodProgram() throws Exception {
        Environment env = testEnv("program P " +
        		"assume true " +
        		"assert arb " +
        		"end 1 = 0 " +
        		"goto 2, 9999");
    }
    
    public void testBooleanStatements() throws Exception {
        try {
            Environment env = testEnv("program P   assert 1");
            fail();
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
        try {
            Environment env = testEnv("program P   assume 1");
            fail();
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
        try {
            Environment env = testEnv("program P   end 1");
            fail();
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        }
        
        testEnv("program P  assume arb");
    }
    
    // from a bug : typing not checked on assignments
    public void testAssignmentTyping() throws Exception {
        
        testEnv("function int i assignable   program P  i := arb");
        
        try {
            Environment env = testEnv("function int i assignable   program P   i := true");
            fail("illegal program: i is int, true is bool");
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
}
