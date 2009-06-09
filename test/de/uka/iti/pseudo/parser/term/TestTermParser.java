/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.parser.term;

import java.io.File;
import java.io.FileNotFoundException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.term.creation.ShuntingYard;

public class TestTermParser {

    public static void main(String[] args) throws Exception {
        
        Environment env = loadBase();
        
        env.dump();
        
        //String term = "a+b^c*d";
        //String term = " - - 5 * - - 5";
        String term = "! -5 = x & y";
		TermParser fp = new TermParser(term, "test.file", 20, 20);

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