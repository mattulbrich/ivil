package de.uka.iti.pseudo.algo;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.util.Util;

public class AlgoVisitor extends DefaultAlgoParserVisitor {

    private String programName;
    private final TermVisitor termVisitor;
    private final Translation translation;
    private final List<String> statements = new ArrayList<String>();
    private final List<String> requirements = new ArrayList<String>();
    private final List<String> ensures = new ArrayList<String>();
    private final IdentifierProducer idProducer = new IdentifierProducer();
    private String firstLine;
    private final boolean refinementMode;

    public AlgoVisitor(Translation translation, boolean refinementMode) {
        this.translation = translation;
        this.refinementMode = refinementMode;
        this.termVisitor = new TermVisitor(translation, statements);
    }

    public List<String> extractProgram(ASTAlgo node) {
        node.jjtAccept(this, null);

        ArrayList<String> result = new ArrayList<String>();
        result.add(firstLine);
        result.addAll(requirements);
        result.addAll(statements);
        result.add(" endOfProgram: ");
        result.addAll(ensures);
        result.add("");
        return result;
    }

    private String visitTermChild(SimpleNode node, int i) {
        return node.jjtGetChild(i).jjtAccept(termVisitor, null);
    }

    private String addDeclarations(SimpleNode node, String mode) {
        int count = node.jjtGetNumChildren();
        List<String> identifiers = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            Node n = node.jjtGetChild(i);
            String val = n.jjtAccept(this, null);
            if (n instanceof ASTIdentifier) {
                identifiers.add(val);
            } else {
                assert n instanceof ASTType;
                for (String string : identifiers) {
                    translation.addFunctionSymbol(string, val, mode);
                }
                identifiers.clear();
            }
        }

        return null;
    }

    private void addSourceLineStatement(SimpleNode node) {
        statements.add(" sourceline " + node.jjtGetFirstToken().beginLine);
    }

    @Override
    public String visit(ASTAlgo node, Object data) {

        programName = visitChild(node, 0);
        String sourceFile = translation.getSourceFile();
        if(sourceFile != null) {
            firstLine = "program " + programName + " source \"" + sourceFile + "\"";
        } else {
            firstLine = ("program " + programName);
        }

        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public String visit(ASTInputDecl node, Object data) {
        return addDeclarations(node, "");
    }

    @Override
    public String visit(ASTOutputDecl node, Object data) {
        return addDeclarations(node, "assignable");
    }

    @Override
    public String visit(ASTVarDecl node, Object data) {
        return addDeclarations(node, "assignable");
    }

    @Override
    public String visit(ASTRequiresDecl node, Object data) {
        requirements.add(" sourceline " + node.jjtGetFirstToken().beginLine);
        requirements.add("  assume " + visitTermChild(node, 0) + " ; \"by requirement\"");
        return null;
    }

    @Override
    public String visit(ASTEnsuresDecl node, Object data) {
        // ensures only in non-ref mode
        if(!refinementMode) {
            ensures.add(" sourceline " + node.jjtGetFirstToken().beginLine);
            ensures.add("  assert " + visitTermChild(node, 0) + " ; \"by ensures\"");
        }
        return null;
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
        int numChildren = node.jjtGetNumChildren();
        String phi = visitTermChild(node, numChildren-1);

        addSourceLineStatement(node);

        // make quantification, the check only in non-ref mode
        if(!refinementMode) {
            StringBuilder quant = new StringBuilder("  assert ");
            for(int i = 0; i < numChildren - 1; i++) {
                quant.append("(\\exists " + visitChild(node, i) + "; ");
            }
            quant.append(phi);
            quant.append(Util.duplicate(")", numChildren-1));
            quant.append(" ; \"assert before choose\"");
            statements.add(quant.toString());
        }

        // make havocs
        Object hint = node.jjtGetValue();
        String annotation;
        if(hint != null) {
            annotation = "; " + Util.addQuotes("witness by " + hint.toString());
        } else {
            annotation = "";
        }
        for (int i = 0; i < numChildren - 1; i++) {
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

        String loopLabel = idProducer.makeIdentifier("loop");
        String bodyLabel = idProducer.makeIdentifier("body");
        String afterLabel = idProducer.makeIdentifier("after");

        addSourceLineStatement(node);
        statements.add("  " + as + " := " + expression);
        statements.add(" " + loopLabel + ":");
        if(!refinementMode) {
            statements.add("  skip LOOPINV, " + inv + ", " + as);
        }
        statements.add("  goto " + bodyLabel + ", " + afterLabel);
        statements.add(" " + bodyLabel + ":");
        statements.add("  assume !" + as + "= emptyset; \"assume condition \"");
        statements.add("  havoc " + with);
        statements.add("  assume " + with + " :: " + as + " ; \"choose element in " + as + "\"");
        statements.add("  " + as + " := " + as + " \\ singleton(" + with + ")");
        visitChild(node, 4);
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
        Object extra = node.jjtGetValue();
        String annotation;
        if(extra != null) {
            annotation = " ; " + Util.addQuotes("lemma by " + extra.toString());
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
            addSourceLineStatement(node);
            String markPoint = (String) node.jjtGetValue();
            String markInvariant = translation.getCouplingInvariant(markPoint);
            String markVariant = translation.getCouplingVariant(markPoint);
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
