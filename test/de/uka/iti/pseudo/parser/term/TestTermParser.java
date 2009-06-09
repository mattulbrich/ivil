package de.uka.iti.pseudo.parser.term;

import java.io.File;
import java.io.FileNotFoundException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.term.ShuntingYard;

public class TestTermParser {

    public static void main(String[] args) throws Exception {
        
        Environment env = loadBase();
        
        TermParser fp = new TermParser("a+b^c*d", "test.file", 20, 20);

        ASTTerm t = fp.parseTerm();
        t.dumpTree();
        
        ShuntingYard.shuntingYard(env, (ASTListTerm) t).dumpTree();
    }

    private static Environment loadBase() throws FileNotFoundException, ParseException, ASTVisitException {
        FileParser fp = new FileParser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File("sys/base.p"));
        return em.getEnvironment();
    }
}