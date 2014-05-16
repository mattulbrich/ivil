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
package de.uka.iti.pseudo.gui;

import java.io.File;

import javax.swing.WindowConstants;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.util.Dump;

public class ShowMainWindow {

    // outdated
    @Deprecated
    public static void main(String[] args) throws Exception {

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Parser fp = new Parser();

        String arg = args.length > 0 ? args[0] : "examples/first.p";

        EnvironmentMaker em = new EnvironmentMaker(fp, new File(arg));
        Environment env = em.getEnvironment();

        Dump.dumpEnv(env);

        Proof proof = new Proof(em.getProblemSequents().get(""), env);

        ProofCenter proofCenter = new ProofCenter(proof, env);
        MainWindow main = proofCenter.getMainWindow();
        main.setSize(600, 600);
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

}
