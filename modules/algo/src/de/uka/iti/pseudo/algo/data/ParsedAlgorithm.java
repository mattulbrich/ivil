/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.algo.ASTStatementBlock;
import de.uka.iti.pseudo.algo.SimpleNode;

public class ParsedAlgorithm {

    private ASTStatementBlock statementBlock;
    private final String name;
    private final Map<String, String> inputVariables = new HashMap<String, String>();
    private final Map<String, String> outputVariables = new HashMap<String, String>();
    private final Map<String, String> localVariables = new HashMap<String, String>();
    private final List<SimpleNode> requirements = new ArrayList<SimpleNode>();
    private final List<SimpleNode> guarantees = new ArrayList<SimpleNode>();
    private final List<String> translation = new ArrayList<String>();

    public enum VarType { INPUT, OUTPUT, LOCAL };

    public ParsedAlgorithm(String name) {
        this.name = name;
    }

    public void addVariableSymbol(String name, String type, VarType mode) {
        switch(mode) {
        case INPUT: inputVariables.put(name, type); break;
        case OUTPUT: outputVariables.put(name, type); break;
        case LOCAL: localVariables.put(name, type); break;
        default: throw new Error("should not be reached");
        }
    }

    public void addRequirement(SimpleNode node) {
        requirements.add(node);
    }

    public void addEnsures(SimpleNode node) {
        guarantees.add(node);
    }

    public void setStatementBlock(ASTStatementBlock node) {
        this.statementBlock = node;
    }

    public void dump() {

        System.out.println("algo " + name);
        for (String var : inputVariables.keySet()) {
            System.out.println("IN " + var + " : " + inputVariables.get(var));
        }

        for (String var : outputVariables.keySet()) {
            System.out.println("OUT " + var + " : " + outputVariables.get(var));
        }

        for (String var : localVariables.keySet()) {
            System.out.println("LOC " + var + " : " + localVariables.get(var));
        }

        for (SimpleNode req : requirements) {
            System.out.println("PRE ");
            req.dump("");
        }

        for (SimpleNode req : guarantees) {
            System.out.println("POST ");
            req.dump("");
        }

        System.out.println("DO");
        statementBlock.dump("");

    }

    public String getName() {
        return name;
    }

    public List<String> getTranslation() {
        return translation;
    }

    public void addDeclarationsTo(ParsedData data) {

        data.addDeclaration("(* input variables for " + name + " *)");
        for (Map.Entry<String, String> en : inputVariables.entrySet()) {
            data.addFunctionSymbol(en.getKey(), en.getValue());
        }

        data.addDeclaration("(* output variables for " + name + " *)");
        for (Map.Entry<String, String> en : outputVariables.entrySet()) {
            data.addFunctionSymbol(en.getKey(), en.getValue(), "assignable");
        }

        data.addDeclaration("(* local variables for " + name + " *)");
        for (Map.Entry<String, String> en : localVariables.entrySet()) {
            data.addFunctionSymbol(en.getKey(), en.getValue(), "assignable");
        }
    }

    public List<SimpleNode> getRequirements() {
        return requirements;
    }

    public List<SimpleNode> getGuarantees() {
        return guarantees;
    }

    public ASTStatementBlock getStatementBlock() {
        return statementBlock;
    }
}



//
//String sourceFile = parsedData.getSourceFile();
//if(sourceFile != null) {
//    firstLine = "program " + programName + " source \"" + sourceFile + "\"";
//} else {
//    firstLine = ("program " + programName);
//}

//requirements.add(" sourceline " + );
//requirements.add("  assume " + visitTermChild(node, 0) + " ; \"by requirement\"");

//// ensures only in non-ref mode
//if(!refinementMode) {
//    ensures.add(" sourceline " + node.jjtGetFirstToken().beginLine);
//    ensures.add("  assert " + visitTermChild(node, 0) + " ; \"by ensures\"");
//}

