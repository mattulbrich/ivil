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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.ProofObligationManager;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.util.Log;

/**
 * This class is used as builder to create problem proof obligations from an
 * environment.
 *
 * A file is parsed into an environment and its problem sequents. One
 * {@link AutomaticProblemProver} is then created per problem in method
 * {@link #createProblemProvers()}
 *
 * @see AutomaticProblemProver
 * @author mattias ulbrich
 */
public class FileProblemProverBuilder {

    /**
     * The file under inspection
     */
    private final File file;

    /**
     * The problem terms extracted from the file. Can be empty or created
     * automatically.
     */
    private final ProofObligationManager proofObligationManager;

    /**
     * The timeout after which the search will be given up.
     */
    private int timeout = -1;

    /**
     * The maximum number of rule applications done.
     */
    private int ruleApplicationLimit = 0;

    /**
     * Relay error messages to source files. (will disappear when result is more
     * elaborate)
     */
    private boolean relayToSource;

    /**
     * Pretty printer for the environment
     */
//    private final PrettyPrint prettyPrint;

    private String[] selectedProofObligations;


    /**
     * Returns the timeout set for this prover. -1 means no timeout.
     *
     * @return the timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout for this file. -1 means no timeout. A positive value a
     * time span in seconds.
     *
     * @param timeout
     *            the timeout to set, a value {@literal >=} -1
     */
    public void setTimeout(int timeout) {
        assert timeout == -1 || timeout > 0 : timeout;
        this.timeout = timeout;
    }

    /**
     * Set the maximum number of rule applications done before giving up with a
     * timeout. 0 means no limit.
     *
     * @param limit
     *            the number of rule applications as limit, a non-negative
     *            value;
     */
    public void setRuleLimit(int limit) {
        assert limit >= 0 : limit;
        this.ruleApplicationLimit = limit;
    }

    /**
     * Instantiates a new automatic file handler.
     *
     * @param file
     *            an ivil input file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException TODO
     */
    public FileProblemProverBuilder(File file) throws EnvironmentException, IOException {
        this.file = file;

        proofObligationManager =
                EnvironmentCreationService.createEnvironmentByExtension(file.toURI().toURL());
    }



    /**
     * check whether this prover relates error messages to source code
     *
     * @return <code>true</code> if it relates error messages to source code.
     */
    public boolean isRelayToSource() {
        return relayToSource;
    }

    /**
     * Set whether this prover relates error messages to source code
     *
     * @param relayToSource
     *            to set
     */
    public void setRelayToSource(boolean relayToSource) {
        this.relayToSource = relayToSource;
    }

    /**
     * Create a list of {@link AutomaticProblemProver} object representing the
     * problem declarations in the file.
     *
     * @see AutomaticProblemProver
     * @return a freshly created list of {@link AutomaticProblemProver}
     */
    public List<AutomaticProblemProver> createProblemProvers() {
        ArrayList<AutomaticProblemProver> result = new ArrayList<AutomaticProblemProver>();
        for(String name : selectedProofObligations) {
            ProofObligation po = proofObligationManager.getProofObligation(name);
            if(po == null) {
                Log.log(Log.WARNING, "TODO");
            } else {
                result.add(new AutomaticProblemProver(file,
                        po, relayToSource, timeout,
                        ruleApplicationLimit));
            }
        }
        return result;
    }

    public void setProofObligations(Object proofObligations) throws EnvironmentException {

        if ("DEFAULT_IF_PRESENT".equals(proofObligations)) {
            if(proofObligationManager.getEnvironment().hasProperty(
                    ProofObligationManager.DEFAULT_PO_PROPERTY)) {
                proofObligations = "DEFAULT";
            } else {
                proofObligations = "ALL";
            }
        }

        if("ALL".equals(proofObligations)) {
            this.selectedProofObligations =
                proofObligationManager.getRelevantProofObligationNames();

        } else if("DEFAULT".equals(proofObligations)) {
            String defaultProperty =
                proofObligationManager.getEnvironment().getProperty(
                    ProofObligationManager.DEFAULT_PO_PROPERTY);

            if(defaultProperty == null) {
                throw new EnvironmentException("TODO");
            }

            this.selectedProofObligations = new String[] { defaultProperty };

        } else if(proofObligations instanceof String[]) {
            this.selectedProofObligations = (String[]) proofObligations;

        } else {
            throw new IllegalArgumentException("(internal) unexpected arg: " + proofObligations);
        }
    }

}
