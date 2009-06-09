package de.uka.iti.pseudo.parser.file;

import java.io.File;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;

public class TestRuleParsing extends TestCase {

    public void testRuleParsing() throws Exception {
        FileParser fp = new FileParser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File("test/de/uka/iti/pseudo/parser/file/ruletest.p"));
        Environment env = em.getEnvironment();
        env.dump();
    }
    
}
