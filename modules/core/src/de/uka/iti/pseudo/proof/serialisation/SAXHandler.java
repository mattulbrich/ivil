/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof.serialisation;

import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.LocalSymbolTable;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.FormatException;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.ProgressIndicator;

// TODO DOC ! !
class SAXHandler extends DefaultHandler {

    private final Environment env;
    private final Proof proof;

    private final StringBuilder content = new StringBuilder();
    private String currentId = "";
    private Attributes attributes;
    private MutableRuleApplication ram;
    private LocalSymbolTable localSymbols;
    private int goalNo = 0;
//	private boolean ignoreExceptions;
    private final ProgressIndicator indicator;
    private int applicationCounter = 0;

    public SAXHandler(Environment env, Proof proof, ProgressIndicator indicator, boolean ignoreExceptions) {
        super();
        this.env = env;
        this.proof = proof;
        this.indicator = indicator;
//        this.ignoreExceptions = ignoreExceptions;
    }

//    private void handleException(String message) throws SAXException {
//    	if(ignoreExceptions) {
//    		Log.log(Log.WARNING, "Ignored exception: " + message);
//    	} else {
//    		throw new SAXException(message);
//    	}
//    }

    @Override
    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {

        Log.enter(uri, localName, name, attributes);

        this.attributes = attributes;
        if(name.equals("ruleApplication")) {
            ram = new MutableRuleApplication();

            // optional id
            currentId = attributes.getValue("id");
            if(currentId == null) {
                currentId = "?";
            }

            // rule
            String ruleName = attributes.getValue("rule");
            assert ruleName != null : "No rule referenced! (forbidden by schema)";

            Rule rule = env.getRule(ruleName);
            if(rule == null) {
                throw new SAXException("No rule by the name " + ruleName + "; id=" + currentId);
            }

            ram.setRule(rule);

            // node (format guaranteed by schema)
            String pathStr = attributes.getValue("path");
            assert pathStr != null : "No path referenced! (forbidden by schema)";

            ProofNode proofNode = parsePath(pathStr);
            if(proofNode == null) {
                throw new SAXException("No proof node for path " + pathStr + "; id=" + currentId);
            }

            ram.setProofNode(proofNode);
            localSymbols = proofNode.getLocalSymbolTable();
        }

        content.setLength(0);
        Log.leave();
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        Log.enter(uri, localName, name);
        try {
            if (name.equals("find")) {

                ram.setFindSelector(new TermSelector(content.toString()));

            } else if (name.equals("assume")) {
                ram.getAssumeSelectors().add(new TermSelector(content.toString()));

            } else if (name.equals("property")) {
                String propname = attributes.getValue("name");
                if (propname == null) {
                    throw new SAXException("No property referenced!");
                }
                ram.getProperties().put(propname, content.toString());

            } else if (name.equals("skip")) {
                goalNo++;

            } else if (name.equals("schemavariable")) {
                String varname = attributes.getValue("name");
                assert varname != null : "No variable name referenced (should be ensured by schema)";

                Term term = null;
                term = TermMaker.makeAndTypeTerm(content.toString(), env, localSymbols, "XML-Import");

                Map<String, Term> schemaVariableMapping = ram
                        .getSchemaVariableMapping();
                if (schemaVariableMapping.containsKey(varname)) {
                    throw new SAXException("schema variable " + varname
                            + " already set");
                }
                schemaVariableMapping.put(varname, term);

            } else if (name.equals("typevariable")) {
                String varname = attributes.getValue("name");
                assert varname != null : "No type variable name referenced (should be ensured by schema)";

                Type type = null;
                type = TermMaker.makeType(content.toString(), env, localSymbols);

                Map<String, Type> typeVariableMapping = ram
                        .getTypeVariableMapping();
                if (typeVariableMapping.containsKey(varname)) {
                    throw new SAXException("type variable " + varname
                            + " already set");
                }

                typeVariableMapping.put(varname, type);

            } else if (name.equals("schemaupdate")) {
                String varname = attributes.getValue("name");
                assert varname != null : "No schema update name referenced (should be ensured by schema)";

                Update upd = null;
                upd = TermMaker.makeAndTypeUpdate(content.toString(), env, localSymbols);

                Map<String, Update> updMap = ram.getSchemaUpdateMapping();
                if (updMap.containsKey(varname)) {
                    throw new SAXException("schema update " + varname
                            + " already set");
                }

                updMap.put(varname, upd);

            } else if (name.equals("ruleApplication")) {
                // matchRuleApp();
                proof.apply(ram, env);
                ram = null;
                localSymbols = null;

                if(indicator != null) {
                    applicationCounter ++;
                    if(applicationCounter % 50 == 0) {
                        indicator.setProgress(applicationCounter);
                    }
                }
            } else if(name.equals("stepcount")) {
                int stepCount = Integer.parseInt(content.toString());
                if(indicator != null) {
                    indicator.setMaximum(stepCount);
                }
            }
        } catch (NumberFormatException e) {
            throwSAXException(content, e);
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

    private void throwSAXException(CharSequence content, Exception e) throws SAXException {
        StringBuilder msg = new StringBuilder();
        msg.append("RuleApp on id ").append(currentId).append(": ");
        msg.append("Cannot parse '").append(content).append("'\n");
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


    // FIXME Can this be invoked more than once for one element?!?!?! YES!!!
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        Log.enter("'" + new String(ch, start, length) + "'");
        Log.log(Log.TRACE, "old content='" + content + "'");
        content.append(ch, start, length);
        Log.log(Log.TRACE, "new content='" + content + "'");
    }

    private ProofNode parsePath(String pathStr) throws SAXException {
        String[] parts = pathStr.split(",");
        ProofNode node = proof.getRoot();
        int index = 0;

        try {
            List<ProofNode> children = node.getChildren();
            while(children != null) {
                int size = children.size();
                if(size > 1) {
                    if(index >= parts.length) {
                        throw new IndexOutOfBoundsException("Path too short, node=" + node.getNumber());
                    }
                    int childIdx = Integer.parseInt(parts[index]);

                    if (childIdx < 0 || childIdx >= size) {
                        throw new IndexOutOfBoundsException("Illegal child "
                                + childIdx + "; size=" + size);
                    }
                    node = children.get(childIdx);
                    index ++;
                } else if(size == 0) {
                    throw new IndexOutOfBoundsException(
                            "Path goes to closed node, node="
                                    + node.getNumber());
                } else {
                    node = children.get(0);
                }

                children = node.getChildren();
            }

            return node;
        } catch (RuntimeException e) {
            throw new SAXException("Illegal path '" + pathStr + "' id=" + currentId, e);
        }

    }

}