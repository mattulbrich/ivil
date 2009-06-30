/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.proof.serialisation;

import java.io.IOException;
import java.io.OutputStream;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;

// TODO: Auto-generated Javadoc
/**
 * This interface specifies the methods which need to be implemented to allow
 * the export of performed proofs into an output stream.
 */
public interface ProofExport {
    
    /**
     * Export a proof to an output stream.
     * 
     * The stream may be assumed to accept data.
     * 
     * @param os the stream to write to
     * @param proof the proof to export
     * @param env the environment of the exported proof
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void exportProof(OutputStream os, Proof proof, Environment env) throws IOException;
    
    
    /**
     * Gets the file extension typical for this format
     * 
     * @return the file extension without leading dot
     */
    public String getFileExtension();
    
    
    /**
     * Gets a short explaining name for this export format.
     * 
     * @return the short description of this export format.
     */
    public String getName();
    
}
