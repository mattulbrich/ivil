package de.uka.iti.pseudo.parser.file;

import java.io.File;
import java.io.FileNotFoundException;

import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class TestFileParser {

    public static void main(String[] args) throws FileNotFoundException,
            ParseException, ASTVisitException {
        FileParser fp = new FileParser();
        EnvironmentMaker em = new EnvironmentMaker(fp, new File(args[0]));
        em.getEnvironment().dump();
        System.out.println(em.getProblemTerm().toString());
    }
}