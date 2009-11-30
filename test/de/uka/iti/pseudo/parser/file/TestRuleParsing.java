package de.uka.iti.pseudo.parser.file;

import java.io.File;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;

public class TestRuleParsing extends TestCaseWithEnv {
    
    static {
        System.setProperty("pseudo.showtypes", "true");
    }

    public void testRuleParsing() throws Exception {
        Parser fp = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File("test/de/uka/iti/pseudo/parser/file/ruletest.p"));
        Environment env = em.getEnvironment();
        env.dump();
    }
    
}
