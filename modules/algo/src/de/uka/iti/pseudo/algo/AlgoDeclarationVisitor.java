/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.algo.data.ParsedAlgorithm;
import de.uka.iti.pseudo.algo.data.ParsedAlgorithm.VarType;
import de.uka.iti.pseudo.algo.data.ParsedData;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

public class AlgoDeclarationVisitor extends DefaultAlgoParserVisitor {

    private final TermVisitor termVisitor;

    private ParsedAlgorithm currentAlgo;

    private final ParsedData parsedData;

    public AlgoDeclarationVisitor(ParsedData parsedData) {
        this.parsedData = parsedData;
        this.termVisitor = new TermVisitor(parsedData);
    }

    private String addVariableDeclarations(SimpleNode node, ParsedAlgorithm.VarType mode) {
        int count = node.jjtGetNumChildren();
        List<String> identifiers = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            Node n = node.jjtGetChild(i);
            String val = n.jjtAccept(termVisitor, null);
            if (n instanceof ASTIdentifier) {
                identifiers.add(val);
            } else {
                assert n instanceof ASTType;
                for (String name : identifiers) {
                    // val is now the type!
                    currentAlgo.addVariableSymbol(name, val, mode);
                }
                identifiers.clear();
            }
        }

        return null;
    }

    @Override
    public String visit(ASTStart node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
                child.jjtAccept(this, data);
        }
        return null;
    }

    @Override
    public String visit(ASTAlgo node, Object data) {
        String programName = visitChild(node, 0);
        currentAlgo = new ParsedAlgorithm(programName);
        parsedData.addAlgo(currentAlgo);

        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public String visit(ASTRefinement node, Object data) {
        // do not handle refinements
        return null;
    }

    @Override
    public String visit(ASTOption node, Object data) {
        @SuppressWarnings("unchecked")
        Pair<Token, Token> setup = (Pair<Token, Token>) node.jjtGetValue();
        parsedData.setOption(setup.fst().image, Util.stripQuotes(setup.snd().image));
        return null;
    }

    @Override
    public String visit(ASTUsesInputDeclaration node, Object data) {
        parsedData.addDeclaration("include \"" + node.jjtGetValue() + "\"");
        return null;
    }

    @Override
    public String visit(ASTUsesInlineDeclaration node, Object data) {
        parsedData.addDeclaration(node.jjtGetValue().toString());
        return null;
    }


    @Override
    public String visit(ASTInputDecl node, Object data) {
        return addVariableDeclarations(node, VarType.INPUT);
    }

    @Override
    public String visit(ASTOutputDecl node, Object data) {
        return addVariableDeclarations(node, VarType.OUTPUT);
    }

    @Override
    public String visit(ASTVarDecl node, Object data) {
        return addVariableDeclarations(node, VarType.LOCAL);
    }

    @Override
    public String visit(ASTRequiresDecl node, Object data) {
        currentAlgo.addRequirement((SimpleNode)node.jjtGetChild(0));
        return null;
    }

    @Override
    public String visit(ASTEnsuresDecl node, Object data) {
        currentAlgo.addEnsures((SimpleNode)node.jjtGetChild(0));
        return null;
    }

    @Override
    public String visit(ASTStatementBlock node, Object data) {
        currentAlgo.setStatementBlock(node);
        return null;
    }

    @Override
    public String visit(ASTAbbreviation node, Object data) {
        String name = visitChild(node, 0);
        String term = node.jjtGetChild(1).jjtAccept(termVisitor, data);
        parsedData.putAbbreviation(name, term);
        return null;
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }


    @Override
    public String visit(ASTAbbrevIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("AlgoDeclarationVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

}
