package de.uka.iti.pseudo.proof.serialisation;

import java.io.IOException;
import java.io.InputStream;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;

// TODO Documentation needed
public interface ProofImport {
    Proof importProof(InputStream is, Proof initialProof, Environment env) throws IOException, ProofException;
    
    boolean acceptsInput(InputStream is) throws IOException;
}
