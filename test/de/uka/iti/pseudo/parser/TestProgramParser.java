package de.uka.iti.pseudo.parser;

import java.io.File;
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
    
    private void failEnv(String string) {
        try {
            Environment env = testEnv("program P   assert 1");
            fail("Environment '" + string + "' should not load");
        } catch (Exception e) {
            if(VERBOSE)
                e.printStackTrace();
        } 
    }
    
    public void testGoodProgram() throws Exception {
        Environment env = testEnv("program P " +
        		"assume true " +
        		"assert arb " +
        		"end 1 = 0 " +
        		"goto 2, 9999");
    }
    
    public void testBooleanStatements() throws Exception {
        failEnv("program P   assert 1");
        failEnv("program P   assume 1");
        failEnv("program P   end 1");
        
        testEnv("program P  assume arb");
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
        failEnv("program P   skip_loopvar %b, 4");
    }
}
