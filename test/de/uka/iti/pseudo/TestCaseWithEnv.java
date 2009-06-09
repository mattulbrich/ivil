package de.uka.iti.pseudo;

import java.io.File;
import java.io.FileNotFoundException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import junit.framework.TestCase;

// TODO Documentation needed
public class TestCaseWithEnv extends TestCase {
    
    protected static Environment env;

    private static void loadEnv() throws FileNotFoundException, ParseException, ASTVisitException {
        FileParser fp = new FileParser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File("test/de/uka/iti/pseudo/testenv.p"));
        env = em.getEnvironment();
    }
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            loadEnv();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected Term makeTerm(String string) throws Exception {
        return TermMaker.makeTerm(string, env);
    }
}
