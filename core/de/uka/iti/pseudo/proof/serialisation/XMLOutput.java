/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof.serialisation;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;

public class XMLOutput {
    
    XMLWriter out;
    
    public XMLOutput(Writer writer) {
        this.out = new XMLWriter(writer); 
    }

    public void export(Proof proof) throws IOException {
        out.append("<?xml version=\"1.0\"?>").newline();
        out.start("proof", "format", "0");
        out.start("info").newline();
        {
            out.start("date").append(new Date().toString()).end().newline();
            out.start("version").append("0.0").end().newline();
            out.start("problem").appendEncoded(proof.getRoot().getSequent().toString()).end().newline();
            out.start("hash").append("to be done").end().newline();
            out.end().newline();
        }
        out.start("steps").newline();

        exportProofNode(proof.getRoot());

        out.end().newline();
        out.end();
        out.flush();
    }

    private void exportProofNode(ProofNode node) throws IOException {
        
        while(node != null) {
            RuleApplication ruleApp = node.getAppliedRuleApp();

            if(ruleApp == null) {
                out.start("skip").end().newline();
                return;
            }

            exportRuleApplication(ruleApp);

            List<ProofNode> children = node.getChildren();
            assert children != null;

            // if there is more than one, use recursion,
            // handle single child-nodes with iteration
            for (int i = 0; i < children.size() - 1; i++) {
                exportProofNode(children.get(i));
            }
            
            if(children.size() > 0) {
                node = children.get(children.size()-1);
            } else {
                node = null;
            }
        }
    }

    private void exportRuleApplication(RuleApplication ruleApp)
            throws IOException {
        
        String ruleName = ruleApp.getRule().getName();
        String nodeNumber = Integer.toString(ruleApp.getNodeNumber());
        out.start("ruleApplication", "rule", ruleName, "node", nodeNumber).newline();
        
        // find
        TermSelector findSelector = ruleApp.getFindSelector();
        if(findSelector != null) {
            out.start("find").append(findSelector.toString()).end().newline();
        }
        
        // assumes
        for(TermSelector sel : ruleApp.getAssumeSelectors()) {
            out.start("assume").append(sel.toString()).end().newline();
        }

        // schema variables
        for(Entry<String, Term> entry : ruleApp.getSchemaVariableMapping().entrySet()) {
            // if(isInteractive(entry.getKey(), ruleApp)) {
            out.start("schemavariable", "name", entry.getKey());
            out.append(entry.getValue().toString(true)).end().newline();
            // }
        }
        
        // type variables
        for(Entry<String, Type> entry : ruleApp.getTypeVariableMapping().entrySet()) {
            out.start("typevariable", "name", entry.getKey());
            out.append(entry.getValue().toString()).end().newline();
        }
        
        // schema updates
        for(Entry<String, Update> entry : ruleApp.getSchemaUpdateMapping().entrySet()) {
            out.start("schemaupdate", "name", entry.getKey());
            out.append(entry.getValue().toString()).end().newline();
        }
        
        // properties
        for (Entry<String, String> entry : ruleApp.getProperties().entrySet()) {
            out.start("property", "name", entry.getKey()).
                appendEncoded(entry.getValue()).end().newline();
        }

        out.end().newline();
    }

//    private boolean isInteractive(String key, RuleApplication ruleApp) {
//        return ruleApp.getProperties().containsKey("interact(" + key + ")");
//    }
}
