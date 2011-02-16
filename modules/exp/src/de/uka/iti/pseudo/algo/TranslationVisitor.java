package de.uka.iti.pseudo.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TranslationVisitor implements AlgoParserVisitor {
    
    private List<String> header = new ArrayList<String>();
    private List<String> statements = new ArrayList<String>();
    private IdentifierProducer idProducer = new IdentifierProducer();
    
    private void addSourceLineStatement(SimpleNode node) {
        statements.add(" sourceline " + node.jjtGetFirstToken().beginLine);
    }
    
    private String visitChild(Node node, int index) {
        return node.jjtGetChild(index).jjtAccept(this, null);
    }
    
    @Override
    public String visit(SimpleNode node, Object data) {
        throw new Error("Should not be called");
    }
    
    @Override
    public String visit(ASTStart node, Object data) {
        header.add("# Automatically created on " + new Date());
        node.childrenAccept(this, data);
        return null;
    }
    
    @Override
    public String visit(ASTUsesDeclaration node, Object data) {
        header.add("include \"" + node.jjtGetValue() + "\"");
        return null;
    }
    
    @Override
    public String visit(ASTAlgo node, Object data) {
        String id = visitChild(node, 0);
        statements.add("program " + id);
        node.childrenAccept(this, data);
        statements.add("");
        return null;
    }

    private String addDeclarations(SimpleNode node) {
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
                    header.add("function " + val + " " + string  + " assignable");
                }
                identifiers.clear();
            }
        }
        
        return null;
    }
    
    @Override
    public String visit(ASTInputDecl node, Object data) {
        return addDeclarations(node);
    }

    @Override
    public String visit(ASTOutputDecl node, Object data) {
        return addDeclarations(node);
    }
    
    @Override
    public String visit(ASTVarDecl node, Object data) {
        return addDeclarations(node);
    }
    
    @Override
    public String visit(ASTStatementBlock node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }
    
    @Override
    public String visit(ASTType node, Object data) {
        StringBuilder ret = new StringBuilder();
        ret.append(visitChild(node, 0));
        if(node.jjtGetNumChildren() > 1) {
            ret.append("(");
            for(int i=1; i < node.jjtGetNumChildren(); i++) {
                if(i > 1) {
                    ret.append(",");
                }
                ret.append(visitChild(node, i));
            }
            ret.append(")");
        }
        return ret.toString();
    }
    
    @Override
    public String visit(ASTAssignmentStatement node, Object data) {
        addSourceLineStatement(node);
        statements.add("  " + visitChild(node, 0) + " := " + visitChild(node, 1));
        return null;
    }
    
    @Override
    public String visit(ASTChooseStatement node, Object data) {
        String id = visitChild(node, 0);
        String phi = visitChild(node,1);
        addSourceLineStatement(node);
        statements.add("  assert (\\exists " + id + "; " + phi + ") ; \"assert before choose\"");
        statements.add("  havoc " + id);
        statements.add("  assume " + phi);
        return null;
    }
    
    @Override
    public String visit(ASTWhileStatement node, Object data) {
        String condition = visitChild(node, 0);
        String invariant = visitChild(node, 1);
        String variant = visitChild(node, 2);

        String loopLabel = idProducer.makeIdentifier("loop");
        String bodyLabel = idProducer.makeIdentifier("body");
        String afterLabel = idProducer.makeIdentifier("after");

        addSourceLineStatement(node);
        statements.add(" " + loopLabel + ":");
        statements.add("  skip_loopinv " + invariant /*+ ", " + variant*/);
        statements.add("  goto " + bodyLabel + ", " + afterLabel);
        statements.add(" " + bodyLabel + ":");
        statements.add("  assume " + condition + "; \"assume condition \"");
        visitChild(node, 3);
        statements.add("  goto " + loopLabel);
        
        statements.add(" " + afterLabel + ":");
        statements.add("  assume $not(" + condition +")");
        return null;
    }
    
    @Override
    public String visit(ASTIterateStatement node, Object data) {
        String expression = visitChild(node, 0);
        String identifier = visitChild(node, 1);

        String loopLabel = idProducer.makeIdentifier("loop");
        String bodyLabel = idProducer.makeIdentifier("body");
        String afterLabel = idProducer.makeIdentifier("after");
        String iter = idProducer.makeIdentifier("$it");
        String iterBefore = idProducer.makeIdentifier("$itBefore");
        
        // TODO TYPING!!
        header.add("function set(prod(node, node)) " + iter + " assignable");
        header.add("function set(prod(node, node)) " + iterBefore + " assignable");

        addSourceLineStatement(node);
        statements.add("  " + iterBefore + " := " + expression);
        statements.add("  " + iter + " := " + iterBefore);
        statements.add(" " + loopLabel + ":");
        statements.add("  skip_loopinv " + iter  + " <: " + iterBefore + "");
        statements.add("  goto " + bodyLabel + ", " + afterLabel);
        statements.add(" " + bodyLabel + ":");
        statements.add("  assume !" + iter + "= emptyset; \"assume condition \"");
        statements.add("  havoc " + identifier);
        statements.add("  assume " + identifier + " :: " + iter);
        statements.add("  " + iter + " := " + iter + " \\ singleton(" + identifier + ")");
        visitChild(node, 2);
        statements.add("  goto " + loopLabel);
        
        statements.add(" " + afterLabel + ":");
        statements.add("  assume " + iter + "= emptyset");
        return null;
    }
    
    @Override
    public String visit(ASTIfStatement node, Object data) {
        String condition = visitChild(node, 0);

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
        statements.add("  assume $not(" + condition + "); \"else\"");
        if (node.jjtGetNumChildren() > 2) {
            visitChild(node, 2);
        }
        statements.add(" " + afterLabel + ":");
        return null;
    }
    
    @Override
    public String visit(ASTAssertStatement node, Object data) {
        addSourceLineStatement(node);
        statements.add("  assert " + visitChild(node, 0));
        return null;
    }
    
    public String visit(ASTIdentifier node, Object data) {
        return (String) node.jjtGetValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String visit(ASTExpression node, Object data) {
        StringBuilder res = new StringBuilder();
        Token t = node.jjtGetFirstToken();
        
        while(t != null && t != node.jjtGetLastToken().next) {
            res.append(t.image).append(" ");
            t = t.next;
        }
        
        return res.toString();
    }
    
    public List<String> getHeader() {
        return header;
    }

    public List<String> getStatements() {
        return statements;
    }

    @Override
    public String visit(ASTMapAssignmentStatement node, Object data) {
        addSourceLineStatement(node);
        String map = visitChild(node, 0);
        String index = visitChild(node, 1);
        String value = visitChild(node, 2);
        statements.add("  " + map + " := write(" + map + ", " + index + ", " + value + ")");
        return null;
    }

}
