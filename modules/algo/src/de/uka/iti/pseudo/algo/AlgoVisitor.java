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

import java.util.List;

import de.uka.iti.pseudo.algo.data.ParsedAlgorithm;
import de.uka.iti.pseudo.algo.data.ParsedData;
import de.uka.iti.pseudo.algo.data.RefinementDeclaration;
import de.uka.iti.pseudo.util.Util;

public class AlgoVisitor extends DefaultAlgoParserVisitor {

    private final TermVisitor termVisitor;
    private final ParsedData parsedData;
    private final ParsedAlgorithm parsedAlgo;
    private final List<String> statements;
    private final IdentifierProducer idProducer = new IdentifierProducer();

    private final boolean refinementMode;

    public AlgoVisitor(ParsedData parsedData, ParsedAlgorithm algo, boolean refinementMode) {
        this.parsedData = parsedData;
        this.parsedAlgo = algo;
        this.refinementMode = refinementMode;
        this.statements = this.parsedAlgo.getTranslation();
        this.termVisitor = new TermVisitor(parsedData, this.statements);
    }

    public void extractProgram() {

        parsedAlgo.addDeclarationsTo(parsedData);

        String sourceFile = parsedData.getSourceFile();
        if(sourceFile != null) {
            statements.add("program " + parsedAlgo.getName() + " source \"" + sourceFile + "\"");
        } else {
            statements.add("program " + parsedAlgo.getName());
        }

        addRequirements();
        parsedAlgo.getStatementBlock().jjtAccept(this, null);
        statements.add(" endOfProgram: ");
        addGuarantees();
        statements.add("");
    }

    private void addRequirements() {
        for (SimpleNode req : parsedAlgo.getRequirements()) {
            addSourceLineStatement(req);
            String exp = req.jjtAccept(termVisitor, null);
            statements.add("  assume " + exp);
        }
    }

    private void addGuarantees() {
        for (SimpleNode gu : parsedAlgo.getGuarantees()) {
            addSourceLineStatement(gu);
            String exp = gu.jjtAccept(termVisitor, null);
            statements.add("  assert " + exp);
        }
    }

    private String visitTermChild(SimpleNode node, int i) {
        return node.jjtGetChild(i).jjtAccept(termVisitor, null);
    }

    private void addSourceLineStatement(SimpleNode node) {
        statements.add(" sourceline " + node.jjtGetFirstToken().beginLine);
    }

    @Override
    public String visit(ASTStatementBlock node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public String visit(ASTType node, Object data) {
       return node.jjtAccept(termVisitor, data);
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visit(ASTAssignmentStatement node, Object data) {
        addSourceLineStatement(node);
        statements.add("  " + visitChild(node, 0) + " := " + visitTermChild(node, 1));
        return null;
    }

    @Override
    public String visit(ASTChooseStatement node, Object data) {
        int identifierCount = (Integer)node.jjtGetValue();
        String phi = visitTermChild(node, identifierCount);

        addSourceLineStatement(node);

        // make quantification, the check only in non-ref mode
        if(!refinementMode) {
            StringBuilder quant = new StringBuilder("  assert ");
            for(int i = 0; i < identifierCount; i++) {
                quant.append("(\\exists " + visitChild(node, i) + "; ");
            }
            quant.append(phi);
            quant.append(Util.duplicate(")", identifierCount));

            String hint = parsedData.retrieveHint(node, "witness", "");
            String annotation;
            if(hint != null) {
                annotation = Util.addQuotes("witness by " + hint);
            } else {
                annotation = "\"assert existence\"";
            }

            quant.append(" ; " + annotation);
            statements.add(quant.toString());
        }

        // make havocs
        String hint = parsedData.retrieveHint(node, "refwitness");
        String annotation;
        if(hint != null) {
            annotation = "; " + Util.addQuotes("witness by " + hint);
        } else {
            annotation = "";
        }
        for (int i = 0; i < identifierCount; i++) {
            statements.add("  havoc " + visitChild(node, i) + annotation);
        }

        // make assumption
        statements.add("  assume " + phi);
        return null;
    }

    @Override
    public String visit(ASTWhileStatement node, Object data) {
        String condition = visitTermChild(node, 0);
        String invariant = visitTermChild(node, 1);
        String variant = visitTermChild(node, 2);

        String loopLabel = idProducer.makeIdentifier("loop");
        String bodyLabel = idProducer.makeIdentifier("body");
        String afterLabel = idProducer.makeIdentifier("after");


        statements.add(" " + loopLabel + ":");
        addSourceLineStatement((SimpleNode) node.jjtGetChild(1));
        if(!refinementMode) {
            statements.add("  skip LOOPINV, " + invariant + ", " + variant);
        }
        addSourceLineStatement(node);
        statements.add("  goto " + bodyLabel + ", " + afterLabel);
        statements.add(" " + bodyLabel + ":");
        statements.add("  assume " + condition + "; \"assume condition \"");
        visitChild(node, 3);
        statements.add("  goto " + loopLabel);

        addSourceLineStatement(node);
        statements.add(" " + afterLabel + ":");
        statements.add("  assume $not(" + condition +")");
        return null;
    }

    @Override
    public String visit(ASTIterateStatement node, Object data) {
        String expression = visitTermChild(node, 0);
        String with = visitTermChild(node, 1);
        String as = visitTermChild(node, 2);
        String inv = visitTermChild(node, 3);
        int last = node.jjtGetNumChildren() - 1;

        String loopLabel = idProducer.makeIdentifier("loop");
        String bodyLabel = idProducer.makeIdentifier("body");
        String afterLabel = idProducer.makeIdentifier("after");

        String hint = parsedData.retrieveHint(node, "");
        String annotation;
        if(hint != null) {
            annotation = "; " + Util.addQuotes("witness by " + hint.toString());
        } else {
            annotation = "";
        }

        addSourceLineStatement(node);
        statements.add("  " + as + " := " + expression);
        statements.add(" " + loopLabel + ":");
        if(!refinementMode) {
            statements.add("  skip LOOPINV, " + inv + ", " + as);
        }
        statements.add("  goto " + bodyLabel + ", " + afterLabel);
        statements.add(" " + bodyLabel + ":");
        statements.add("  assume !" + as + "= emptyset; \"assume condition \"");
        statements.add("  havoc " + with + annotation);
        statements.add("  assume " + with + " :: " + as + " ; \"choose element in " + as + "\"");
        statements.add("  " + as + " := " + as + " \\ singleton(" + with + ")");
        visitChild(node, last);
        statements.add("  goto " + loopLabel);

        statements.add(" " + afterLabel + ":");
        statements.add("  assume " + as + "= emptyset");
        return null;
    }

    @Override
    public String visit(ASTIfStatement node, Object data) {
        String condition = visitTermChild(node, 0);

        String thenLabel = idProducer.makeIdentifier("then");
        String elseLabel = idProducer.makeIdentifier("else");
        String afterLabel = idProducer.makeIdentifier("after");

        addSourceLineStatement(node);
        statements.add("  goto " + thenLabel + ", " + elseLabel);
        statements.add(" " + thenLabel + ":");
        statements.add("  assume " + condition + "; \"then\"");
        visitChild(node, 1);
        statements.add("  goto " + afterLabel);
        statements.add(" " + elseLabel + ":");
        if (node.jjtGetNumChildren() > 2) {
            addSourceLineStatement((SimpleNode) node.jjtGetChild(2));
            statements.add("  assume $not(" + condition + "); \"else\"");
            visitChild(node, 2);
        } else {
            // no else: goto "end" as far as line number is concerned
            statements.add(" sourceline " + node.jjtGetLastToken().beginLine);
            statements.add("  assume $not(" + condition + "); \"else\"");
        }
        statements.add(" " + afterLabel + ":");
        return null;
    }

    @Override
    public String visit(ASTReturnStatement node, Object data) {
        addSourceLineStatement(node);
        statements.add("  goto endOfProgram ; \"Return Statement\"");
        return null;
    }

    @Override
    public String visit(ASTInlineStatement node, Object data) {
        addSourceLineStatement(node);
        statements.add(node.jjtGetValue().toString());
        return null;
    }

    @Override
    public String visit(ASTAssertStatement node, Object data) {
        String condition = visitTermChild(node, 0);
        if(!refinementMode) {
            addSourceLineStatement(node);
            statements.add("  assert " + condition);
        }
        return null;
    }

    @Override
    public String visit(ASTAssumeStatement node, Object data) {
        addSourceLineStatement(node);
        statements.add("  assume " + visitTermChild(node, 0));
        return null;
    }

    @Override
    public String visit(ASTNoteStatement node, Object data) {
        String expression = visitTermChild(node, 0);
        addSourceLineStatement(node);

        String hint = parsedData.retrieveHint(node, "");
        String annotation;
        if(hint != null) {
            annotation = " ; " + Util.addQuotes("lemma by " + hint);
        } else {
            annotation = "";
        }
        statements.add("  assert " + expression + annotation);

        if(!refinementMode) {
            statements.add("  assume " + expression + " ; \"use lemma\"");
        }
        return null;
    }

    @Override
    public String visit(ASTMarkStatement node, Object data) {
        if(refinementMode) {
            RefinementDeclaration refDecl = parsedData.getRefinementDeclartion();
            addSourceLineStatement(node);
            String markPoint = (String) node.jjtGetValue();
            String markInvariant = refDecl.getCouplingInvariant(markPoint);
            String markVariant = refDecl.getCouplingVariant(markPoint);
            statements.add(String.format("  skip MARK, %s, %s, %s ; \"marking stone\"",
                    markPoint, markInvariant, markVariant));
        }
        return null;
    }

    @Override
    public String visit(ASTMapAssignmentStatement node, Object data) {
        addSourceLineStatement(node);
        String map = visitTermChild(node, 0);
        String index = visitTermChild(node, 1);
        String value = visitTermChild(node, 2);
        statements.add("  " + map + " := $store(" + map + ", " + index + ", " + value + ")");
        return null;
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("AlgoVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

}
