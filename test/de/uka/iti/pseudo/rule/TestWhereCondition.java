package de.uka.iti.pseudo.rule;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.rule.where.Typing;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class TestWhereCondition extends TestCase {

    private static Environment env;

    private static void loadEnv() throws FileNotFoundException, ParseException, ASTVisitException {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        FileParser fp = new FileParser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File("sys/base.p"));
        env = em.getEnvironment();
    }
    
    static {
        try {
            loadEnv();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testTryTyping() throws Exception {
        Typing typing = new Typing();
        Term t1 = TermMaker.makeTerm("%a as int", env);
        Term t2 = TermMaker.makeTerm("%a + %b", env);
        typing.tryToApplyTo(new Term [] {t1});
        
        try {
            typing.tryToApplyTo(new Term[] {t1,t1});
            fail("should fail");
        } catch(RuleException ex) {
        }
        
        try {
            typing.tryToApplyTo(new Term[] {t2});
            fail("should fail");
        } catch(RuleException ex) {
        }
    }
}
