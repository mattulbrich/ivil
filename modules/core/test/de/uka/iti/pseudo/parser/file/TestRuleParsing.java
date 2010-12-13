/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.net.URL;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;

public class TestRuleParsing extends TestCaseWithEnv {
    
    static {
        System.setProperty("pseudo.showtypes", "true");
    }

    public void testRuleParsing() throws Exception {
        Parser fp = new Parser();
        URL url = getClass().getResource("ruletest.p");
        if(url == null)
            throw new Exception("ruletest.p not found");
        EnvironmentMaker em = new EnvironmentMaker(fp, url);
        Environment env = em.getEnvironment();
        env.dump();
    }
    
}
