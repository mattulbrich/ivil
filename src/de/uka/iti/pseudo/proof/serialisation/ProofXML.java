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
import java.io.InputStream;
import java.nio.ByteBuffer;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.RuleException;

/**
 * This class can be used to save and load {@link Proof}s to XML documents.
 * 
 * TODO perhaps introduce an Interface ProofSerialisation to allow other formats
 * eventually
 * 
 * TODO find required methods and do it later
 */

//interface ProofSerialisation {
//    ByteBuffer exportProof(Proof proof, Environment env) throws IOException;
//    Proof importProof(ByteBuffer byteBuffer, Environment env) throws IOException, ProofException;
//    boolean acceptsInput(ByteBuffer byteBuffer) throws IOException;
//}

public class ProofXML implements ProofImport, ProofExport {
    
    private Environment env;

    public Object exportRuleApplication(RuleApplication ruleApp) {
        // TODO Implement ProofXML.exportRuleApplication
        
        // allow the where clauses to possibly drop additional stuff
        RuleApplication ra = null;
        for (String key : ra.getProperties().keySet()) {
            System.out.println(ra.getProperties().get(key));
        }
        return null;
    }
    
    public RuleApplication importRuleApplication(Object xmlElement) throws RuleException {
        // TODO Implement ProofXML.importRuleApplication
        
        // allow the where clauses to read their properties first!
        RuleApplicationMaker ram = new RuleApplicationMaker();
        
        // ram.putWhereProperty()
        return null;
    }

    public boolean acceptsInput(InputStream is)  throws IOException {
        // TODO Implement ProofXML.acceptsInput
        // TODO method documentation
        return false;
    }

    public Proof importProof(InputStream is, Proof initialProof, Environment env)  throws IOException, ProofException {
        // TODO Implement ProofXML.importProof
        // TODO method documentation
        return null;
    }

    public ByteBuffer exportProof(Proof proof, Environment env)
            throws IOException {
        // TODO Implement ProofXML.exportProof
        // TODO method documentation
        return null;
    }

}
