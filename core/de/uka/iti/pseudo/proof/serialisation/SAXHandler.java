package de.uka.iti.pseudo.proof.serialisation;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.FormatException;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Log;

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
        
        Log.enter(uri, localName, name, attributes);
        
        this.attributes = attributes;
        if(name.equals("ruleApplication")) {
            ram = new RuleApplicationMaker(env);
            
            // rule
            String ruleName = attributes.getValue("rule");
            if(ruleName == null)
                throw new SAXException("No rule referenced!");
            Rule rule = env.getRule(ruleName);
            if(rule == null)
                throw new SAXException("No rule by the name " + ruleName);
            ram.setRule(rule);
        }
        
        Log.leave();
    }
    
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        Log.enter(uri, localName, name);
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
        
        Log.leave();
    }
    
    // TODO Does this really check from which side the terms come from?!
    private void matchRuleApp() throws ProofException {
        ProofNode goal = ram.getProofNode();
        Sequent seq = goal.getSequent();
        List<TermSelector> assumeSelectors = ram.getAssumeSelectors();
        
        LocatedTerm ruleFindClause = ram.getRule().getFindClause();
        if(ruleFindClause != null) {
            Term t1 = ram.getFindSelector().selectSubterm(seq);
            Term t2 = ruleFindClause.getTerm();
            ram.getTermUnification().leftUnify(t2, t1);
        }
        
        for(int i = 0; i < assumeSelectors.size(); i++) {
            Term t1 = assumeSelectors.get(i).selectSubterm(seq);
            Term t2 = ram.getRule().getAssumptions().get(i).getTerm();
            ram.getTermUnification().leftUnify(t2, t1);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        content = new String(ch, start, length);
    }
    
}