package de.uka.iti.pseudo.proof.serialisation;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.FormatException;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Log;

// TODO DOC ! !
class SAXHandler extends DefaultHandler {
    
    private Environment env;
    private Proof proof;
    
    private String content = "";
    private Attributes attributes;
    private MutableRuleApplication ram;
    private int goalNo = 0;
//	private boolean ignoreExceptions;
    
    public SAXHandler(Environment env, Proof proof, boolean ignoreExceptions) {
        super();
        this.env = env;
        this.proof = proof;
//        this.ignoreExceptions = ignoreExceptions;
    }
    
//    private void handleException(String message) throws SAXException {
//    	if(ignoreExceptions) {
//    		Log.log(Log.WARNING, "Ignored exception: " + message);
//    	} else {
//    		throw new SAXException(message);
//    	}
//    }

    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {
        
        Log.enter(uri, localName, name, attributes);
        
        this.attributes = attributes;
        if(name.equals("ruleApplication")) {
            ram = new MutableRuleApplication();
            
            // rule
            String ruleName = attributes.getValue("rule");
            assert ruleName != null : "No rule referenced! (forbidden by schema)";
            
            Rule rule = env.getRule(ruleName);
            if(rule == null)
                throw new SAXException("No rule by the name " + ruleName);
            
            ram.setRule(rule);
            
            // node (format guaranteed by schema)
            String nodeStr = attributes.getValue("node");
            assert nodeStr != null : "No rule referenced! (forbidden by schema)";
            
            int nodeNo = Integer.parseInt(nodeStr);
            ProofNode proofNode = proof.getGoalbyNumber(nodeNo);
            if(proofNode == null)
                throw new SAXException("No proof node of number " + nodeNo);
            
            ram.setProofNode(proofNode); 
        }
        
        Log.leave();
    }
    
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        Log.enter(uri, localName, name);
        try {
            if (name.equals("find")) {

                ram.setFindSelector(new TermSelector(content));

            } else if (name.equals("assume")) {
                ram.getAssumeSelectors().add(new TermSelector(content));

            } else if (name.equals("property")) {
                String propname = attributes.getValue("name");
                if (propname == null)
                    throw new SAXException("No property referenced!");
                ram.getProperties().put(propname, content);

            } else if (name.equals("skip")) {
                goalNo++;

            } else if (name.equals("schemavariable")) {
                String varname = attributes.getValue("name");
                assert varname != null : "No variable name referenced (should be ensured by schema)";

                Term term = null;
                term = TermMaker.makeAndTypeTerm(content, env, "XML-Import");

                Map<String, Term> schemaVariableMapping = ram
                        .getSchemaVariableMapping();
                if (schemaVariableMapping.containsKey(varname))
                    throw new SAXException("schema variable " + varname
                            + " already set");
                schemaVariableMapping.put(varname, term);

            } else if (name.equals("typevariable")) {
                String varname = attributes.getValue("name");
                assert varname != null : "No type variable name referenced (should be ensured by schema)";

                Type type = null;
                type = TermMaker.makeType(content, env);

                Map<String, Type> typeVariableMapping = ram
                        .getTypeVariableMapping();
                if (typeVariableMapping.containsKey(varname))
                    throw new SAXException("type variable " + varname
                            + " already set");

                typeVariableMapping.put(varname, type);

            } else if (name.equals("schemaupdate")) {
                String varname = attributes.getValue("name");
                assert varname != null : "No schema update name referenced (should be ensured by schema)";

                Update upd = null;
                upd = TermMaker.makeAndTypeUpdate(content, env);

                Map<String, Update> updMap = ram.getSchemaUpdateMapping();
                if (updMap.containsKey(varname))
                    throw new SAXException("schema update " + varname
                            + " already set");

                updMap.put(varname, upd);

            } else if (name.equals("ruleApplication")) {
                // matchRuleApp();
                proof.apply(ram, env);
                ram = null;

            }
        } catch (FormatException e) {
            throwSAXException(content, e);
        } catch (ParseException e) {
            throwSAXException(content, e);
        } catch (ASTVisitException e) {
            throwSAXException(content, e);
        } catch (ProofException e) {
            if (Log.isLogging(Log.WARNING)) {
                Dump.dumpRuleApplication(ram);
            }
            throwSAXException(content, e);
        }
        
        Log.leave();
    }
    
    private void throwSAXException(String content, Exception e) throws SAXException {
        StringBuilder msg = new StringBuilder();
        if(ram != null && ram.getProofNode() != null) {
            msg.append("RuleApp on ").append(ram.getProofNode().getNumber()).
                    append(": ");
        }
        
        msg.append("Cannot parse '").append(content).append("'");
        SAXException saxException = new SAXException(msg.toString(), e);
        saxException.initCause(e);
        throw saxException;
    }

    // TODO Does this really check from which side the terms come from?!
//    private void matchRuleApp() throws ProofException {
//        ProofNode goal = ram.getProofNode();
//        Sequent seq = goal.getSequent();
//        List<TermSelector> assumeSelectors = ram.getAssumeSelectors();
//        
//        LocatedTerm ruleFindClause = ram.getRule().getFindClause();
//        if(ruleFindClause != null) {
//            Term t1 = ram.getFindSelector().selectSubterm(seq);
//            Term t2 = ruleFindClause.getTerm();
//            ram.getTermUnification().leftUnify(t2, t1);
//        }
//        
//        for(int i = 0; i < assumeSelectors.size(); i++) {
//            Term t1 = assumeSelectors.get(i).selectSubterm(seq);
//            Term t2 = ram.getRule().getAssumptions().get(i).getTerm();
//            ram.getTermUnification().leftUnify(t2, t1);
//        }
//    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        content = new String(ch, start, length);
    }
    
}