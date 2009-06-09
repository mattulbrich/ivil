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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ParseException;
import de.uka.iti.pseudo.proof.FormatException;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMaker;

/**
 * This class can be used to save and load {@link Proof}s to XML documents.
 * 
 * It implements both the import and the export interface.
 * 
 * Import is however handled by {@link SAXHandler} which constructs
 * {@link RuleApplicationMaker} from the XML content.
 * 
 * Export is handled using a {@link XMLWriter}, a very simple XML export
 * facility.
 */

public class ProofXML implements ProofImport, ProofExport {
    
    public boolean acceptsInput(InputStream is)  throws IOException {
        return true;
    }

    public void importProof(InputStream is, Proof proof, Environment env)  throws IOException, ProofException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SAXHandler handler = new SAXHandler(env, proof);
            parser.parse(is, handler);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }
    
    public void exportProof(OutputStream os, Proof proof, Environment env)
            throws IOException {
        
        XMLWriter w = new XMLWriter(new OutputStreamWriter(os));
        
        w.append("<?xml version=\"1.0\"?>").newline();
        w.start("pseudoproof", "format", "0");
        w.start("info").newline();
        {
            w.start("date").append(new Date().toString()).end().newline();
            w.start("version").append("0.0").end().newline();
            w.start("problem").append(
                    "<![CDATA[" + proof.getRoot().getSequent().toString()
                            + "]]>").end().newline();
            w.start("hash").append("to be done").end().newline();
            w.end().newline();
        }
        w.start("steps").newline();
        
        exportProofNode(w, proof.getRoot());
        
        w.end().newline();
        w.end();
        w.flush();
    }

    private void exportProofNode(XMLWriter w, ProofNode node) throws IOException {
        
        while(node != null) {
            RuleApplication ruleApp = node.getAppliedRuleApp();

            if(ruleApp == null) {
                w.start("skip").end().newline();
                return;
            }

            exportRuleApplication(w, ruleApp);

            List<ProofNode> children = node.getChildren();
            assert children != null;

            // if there is more than one, use recursion,
            // handle single child-nodes with iteration
            for (int i = 0; i < children.size() - 1; i++) {
                exportProofNode(w, children.get(i));
            }
            
            if(children.size() > 0) {
                node = children.get(children.size()-1);
            } else {
                node = null;
            }
        }
    }

    private void exportRuleApplication(XMLWriter w, RuleApplication ruleApp)
            throws IOException {
        
        String ruleName = ruleApp.getRule().getName();
        w.start("ruleApplication", "rule", ruleName).newline();
        
        // properties
        for (Entry<String, String> entry : ruleApp.getProperties().entrySet()) {
            w.start("property", "name", entry.getKey()).
                appendEncoded(entry.getValue()).end().newline();
        }
        
        // finds
        w.start("find").append(ruleApp.getFindSelector().toString()).end().newline();
        
        // assumes
        for(TermSelector sel : ruleApp.getAssumeSelectors()) {
            w.start("assume").append(sel.toString()).end().newline();
        }
        
        // type variables
//        for(Entry<String, Type> entry : ruleApp.getTypeVariableMapping().entrySet()) {
//            w.start("typevariable", "name", entry.getKey());
//            w.append(entry.getValue().toString()).end().newline();
//        }
        
        // schema variables
        for(Entry<String, Term> entry : ruleApp.getSchemaVariableMapping().entrySet()) {
            if(isInteractive(entry.getKey(), ruleApp)) {
                w.start("schemavariable", "name", entry.getKey());
                w.append(entry.getValue().toString(true)).end().newline();
            }
        }
        
        // schema modalities
        for(Entry<String, Modality> entry : ruleApp.getSchemaModalityMapping().entrySet()) {
            if(isInteractive(entry.getKey(), ruleApp)) {
                w.start("schemamodality", "name", entry.getKey());
                w.append(entry.getValue().toString(true)).end().newline();
            }
        }

        w.end().newline();
    }

    private boolean isInteractive(String key, RuleApplication ruleApp) {
        return ruleApp.getProperties().containsKey("interact(" + key + ")");
    }

    public String getFileExtension() {
        return "pxml";
    }

    public String getName() {
        return "PXML -- proof XML file";
    }

}

class SAXHandler extends DefaultHandler {
    
    private Environment env;
    private Proof proof;
    
    private String content = "";
    private Attributes attributes;
    private RuleApplicationMaker ram;
    private int goalNo = 0;
    
    public SAXHandler(Environment env, Proof proof) {
        super();
        this.env = env;
        this.proof = proof;
    }

    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {
        this.attributes = attributes;
        if(name.equals("ruleApplication")) {
            ram = new RuleApplicationMaker();
            
            // rule
            String ruleName = attributes.getValue("rule");
            if(ruleName == null)
                throw new SAXException("No rule referenced!");
            Rule rule = env.getRule(ruleName);
            if(rule == null)
                throw new SAXException("No rule by the name " + ruleName);
            ram.setRule(rule);
            
            // goalno
            ram.setGoalNumber(goalNo);
        }            
    }
    
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        System.out.println("ende: " + name);
        if(name.equals("find")) {
            try {
                ram.setFindSelector(new TermSelector(content));
            } catch (FormatException e) {
                throw new SAXException(e);
            }
            
        } else if(name.equals("assume")) {
            try {
                ram.pushAssumptionSelector(new TermSelector(content));
            } catch (FormatException e) {
                throw new SAXException(e);
            }
            
        } else if(name.equals("property")) {
            String propname = attributes.getValue("name");
            if(propname == null)
                throw new SAXException("No property referenced!");
            ram.getProperties().put(propname, content);
            
        } else if(name.equals("skip")) {
            goalNo ++;
            
        } else if(name.equals("schemavariable")) {
            String varname = attributes.getValue("name");
            if(varname == null)
                throw new SAXException("No variable name referenced!");
            Term term;
            try {
                term = TermMaker.makeAndTypeTerm(content, env, "XML-Import");
            } catch (ParseException e) {
                throw new SAXException("Cannot parse term", e);
            } catch (ASTVisitException e) {
                throw new SAXException("Cannot parse term", e);
            }
            
            try {
                ram.getTermUnification().addInstantiation(new SchemaVariable(varname, Environment.getIntType()), term);
            } catch (TermException e) {
                throw new SAXException(e);
            }
            
        } else if(name.equals("ruleApplication")) {
            try {
                matchRuleApp();
                proof.apply(ram, env);
            } catch (ProofException e) {
                throw new SAXException(e);
            }
        }
    }
    
    private void matchRuleApp() throws ProofException {
        ProofNode goal = proof.getGoal(ram.getGoalNumber());
        Sequent seq = goal.getSequent();
        List<TermSelector> assumeSelectors = ram.getAssumeSelectors();
        
        Term t1 = ram.getFindSelector().selectSubterm(seq);
        Term t2 = ram.getRule().getFindClause().getTerm();
        ram.getTermUnification().leftUnify(t2, t1);
        
        for(int i = 0; i < assumeSelectors.size(); i++) {
            t1 = assumeSelectors.get(i).selectSubterm(seq);
            t2 = ram.getRule().getAssumptions().get(i).getTerm();
            ram.getTermUnification().leftUnify(t2, t1);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        content = new String(ch, start, length);
    }
    
}
