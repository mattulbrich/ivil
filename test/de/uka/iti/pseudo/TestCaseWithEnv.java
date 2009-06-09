package de.uka.iti.pseudo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;

// TODO Documentation needed
public class TestCaseWithEnv extends TestCase {
    
    protected static Environment env;
    
    protected static void loadEnv() throws FileNotFoundException, ParseException, ASTVisitException {
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
    
    protected static Term makeTerm(String string) throws Exception {
        return TermMaker.makeAndTypeTerm(string, env, "*test*");
    }

}
