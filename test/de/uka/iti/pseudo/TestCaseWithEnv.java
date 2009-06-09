package de.uka.iti.pseudo;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

// TODO Documentation needed
public class TestCaseWithEnv extends TestCase {
    
    protected static Environment env;
    
    protected static void loadEnv() throws FileNotFoundException, ParseException, ASTVisitException {
        Parser fp = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File("test/de/uka/iti/pseudo/testenv.p"));
        env = em.getEnvironment();
    }
    
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            loadEnv();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    protected static Term makeTerm(String string) throws Exception {
        return TermMaker.makeAndTypeTerm(string, env, "*test*");
    }

}
