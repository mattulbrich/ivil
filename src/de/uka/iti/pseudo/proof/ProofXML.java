/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.proof;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;

/**
 * This class can be used to save and load {@link Proof}s to XML documents.
 * 
 * TODO perhaps introduce an Interface ProofSerialisation to allow other formats
 * eventually
 * 
 * TODO find required methods and do it later
 */

interface ProofSerialisation {
    ByteBuffer exportProof(Proof proof, Environment env) throws IOException;
    Proof importProof(ByteBuffer byteBuffer, Environment env) throws IOException, ProofException;
    boolean acceptsInput(ByteBuffer byteBuffer) throws IOException;
}

public class ProofXML {
    
    private Environment env;

    public Object exportRuleApplication(RuleApplication ruleApp) {
        // TODO Implement ProofXML.exportRuleApplication
        
        // allow the where clauses to possibly drop additional stuff
        RuleApplication ra = null;
        for (String key : ra.getWherePropertyNames()) {
            System.out.println(ra.getWhereProperty(key));
        }
        return null;
    }
    
    public RuleApplication importRuleApplication(Object xmlElement) throws RuleException {
        // TODO Implement ProofXML.importRuleApplication
        
        // allow the where clauses to read their properties first!
        RuleApplicationMaker ram = null;
        
        // ram.putWhereProperty()
        return null;
    }

}
