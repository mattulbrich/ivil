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
package de.uka.iti.pseudo.proof.serialisation;

import java.io.IOException;
import java.io.InputStream;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;

/**
 * This interface specifies the methods which need to be implemented to allow
 * the import of saved proofs onto a problem.
 */
public interface ProofImport {

    /**
     * Retrieve a proof from an inputstream and store it into a proof object.
     * 
     * If the import fails, it may have already have changed the proof, that is
     * ok.
     * 
     * The proof is locked for the present thread, no interfering of any other
     * thread is possible.
     * 
     * @param is
     *            the inputstream from which the data should be retrieved
     * @param proof
     *            the begun proof, it contains only a root node
     * @param env
     *            the environment of the proof
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ProofException
     *             Signals that something went wrong during proof processing
     */
    public void importProof(@NonNull InputStream is,
            @NonNull Proof proof, @NonNull Environment env)
            throws IOException, ProofException;

    /**
     * Perform a simple test on the input stream whether its data format is
     * correct.
     * 
     * Typically the first bytes are checked for magic code or similar.
     * 
     * @param is
     *            input stream to be checked
     * @return true if the stream is likely to match this import format
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean acceptsInput(InputStream is) throws IOException;
}
