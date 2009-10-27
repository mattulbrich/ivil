package de.uka.iti.pseudo;

import java.io.File;
import java.io.StringReader;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

// TODO Documentation needed
public class TestCaseWithEnv extends TestCase {
    
    protected static final Environment DEFAULT_ENV = loadEnv();
    
    protected Environment env = DEFAULT_ENV;
    
    public static final boolean VERBOSE = Boolean.valueOf(System.getProperty("pseudo.test.verbose", "true"));
    
    protected static Environment loadEnv() {
        try {
            Parser fp = new Parser();
            EnvironmentMaker em = new EnvironmentMaker(fp, new File("test/de/uka/iti/pseudo/testenv.p"));
            return em.getEnvironment();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }
    
    protected Term makeTerm(String string) throws Exception {
        return TermMaker.makeAndTypeTerm(string, env, "*test*");
    }
    
    protected static Environment makeEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "test");
        Environment env = em.getEnvironment();
        return env;
    }

}
