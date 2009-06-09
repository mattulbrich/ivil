package de.uka.iti.pseudo.proof.serialisation;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;

// TODO Documentation needed
public interface ProofExport {
    ByteBuffer exportProof(Proof proof, Environment env) throws IOException;
}
