/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.Callable;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.ProofObligationManager;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.serialisation.ProofImport;
import de.uka.iti.pseudo.proof.serialisation.ProofXML;



/**
 * This is called to load and check a proof for a problem.
 *
 * For a problem file "file.p", the proof "file.pxml" is loaded.
 */
public class AutomaticProblemChecker implements Callable<Result> {

    private final File file;
    private final String proofFileName;

    public AutomaticProblemChecker(File file) {
        this.file = file;
        this.proofFileName = file.getAbsolutePath() + "xml";
    }

    @Override
    public Result call() throws Exception {
        Parser parser = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(parser, file);
        Environment env = em.getEnvironment();
        Map<String, ProofObligation> problems = em.getProofObligations();

        String defaultPO = env.getProperty(ProofObligationManager.DEFAULT_PO_PROPERTY);
        if (defaultPO == null) {
            throw new ProofException("For automatic proof checking, the environment must define" +
                    "a default proof obligation.");
        }

        ProofObligation proofObl = problems.get(defaultPO);
        if(proofObl == null) {
            throw new ProofException("There is no proof obligation that goes by this name: " +
                    defaultPO);
        }

        Proof proof = proofObl.initProof();

        FileInputStream fis = new FileInputStream(proofFileName);

        ProofImport proofImp = new ProofXML();
        proofImp.importProof(fis, proof, env, null);

        if(proof.hasOpenGoals()) {
            return new Result(false, file, defaultPO,
                proof.getOpenGoals().size() + " goal(s) remain open (proof from " +
                    proofFileName + ")");
        } else {
            return new Result(true, file, defaultPO,
                    "Proof loaded from " + proofFileName);
        }
    }

}
