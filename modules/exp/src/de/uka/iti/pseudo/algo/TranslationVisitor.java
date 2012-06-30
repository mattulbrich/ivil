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
import java.util.Date;
import java.util.List;

import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;

/**
 * The TranslationVisitor does the toplevel translation work.
 *
 * It delegates part of the translation to the TermVisitor, AlgoVisitor,
 * JavaVisitor, MethodRefVisitor.
 */
public class TranslationVisitor extends DefaultAlgoParserVisitor {

    private final List<String> programs = new ArrayList<String>();

    private final Translation translation;

    private final boolean refinementMode;

    private final TermVisitor termVisitor;

    private String problem = null;

    public TranslationVisitor(Translation translation, boolean refinementMode) {
        this.translation = translation;
        this.refinementMode = refinementMode;
        this.termVisitor = new TermVisitor(translation);
    }

    public List<String> getPrograms() {
        return programs;
    }

    @Override
    public String visit(ASTStart node, Object data) {
        translation.addDeclaration("# Automatically created on " + new Date());

        // Order of execution:
        // 1. declarations, abbrevs
        // 2. refinement
        // 3. algo and everyhing else

        List<Node> decls = new ArrayList<Node>();
        List<Node> ref = new ArrayList<Node>();
        List<Node> remain = new ArrayList<Node>();

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (child instanceof ASTDeclarationBlock || child instanceof ASTAbbreviation) {
                decls.add(child);
            } else if(child instanceof ASTRefinement) {
                ref.add(child);
            } else {
                remain.add(child);
            }
        }

        for (Node n : decls) {
            n.jjtAccept(this, data);
        }

        for (Node n : ref) {
            n.jjtAccept(this, data);
        }

        for (Node n : remain) {
            n.jjtAccept(this, data);
        }

        if(problem  != null) {
            programs.add("");
            programs.add("problem " + problem);
        }

        return null;
    }

    @Override
    public String visit(ASTUsesInputDeclaration node, Object data) {
        translation.addDeclaration("include \"" + node.jjtGetValue() + "\"");
        return null;
    }

    @Override
    public String visit(ASTUsesInlineDeclaration node, Object data) {
        translation.addDeclaration(node.jjtGetValue().toString());
        return null;
    }

    @Override
    public String visit(ASTAbbreviation node, Object data) {
        String name = visitChild(node, 0);
        String term = node.jjtGetChild(1).jjtAccept(termVisitor, data);
        translation.putAbbreviation(name, term);
        return null;
    }

    @Override
    public String visit(ASTAbbrevIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visit(ASTAlgo node, Object data) {

        AlgoVisitor visitor = new AlgoVisitor(translation, refinementMode);

        programs.addAll(visitor.extractProgram(node));
        return null;
    }


    @Override
    public String visit(ASTRefinement node, Object data) {
        if(refinementMode) {
            RefinementVisitor javaVisitor = new RefinementVisitor(translation);
            problem = node.jjtAccept(javaVisitor, data);
        }
        return null;
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("TranslationVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

}
