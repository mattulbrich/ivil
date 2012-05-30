/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.io.File;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;

/**
 * This little application lists the verbosity level of all rules of all files
 * given on command line.
 */
public class VerbosityListing {

    public static void main(String[] args) throws Exception {
        
        if(args.length == 0) {
            help();
            System.exit(0);
        }
        
        Parser p = new Parser();
        for (String arg : args) {
            EnvironmentMaker em = new EnvironmentMaker(p, new File(arg));
            Environment env = em.getEnvironment();
            
            for (Rule rule : env.getLocalRules()) {
                String verbLevel= rule.getProperty(RuleTagConstants.KEY_VERBOSITY);
                if(verbLevel == null)
                    verbLevel ="0";
                System.out.println(rule.getName() + " " + verbLevel);
            }
        }

    }

    private static void help() {
        System.out.println("This little application lists the verbosity level of rules");
        System.out.println("Arguments: .p files to scan for rule definitions");
    }
}
