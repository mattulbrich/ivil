package de.uka.iti.pseudo.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TranslationVisitor implements AlgoParserVisitor {
    
    private static Map<String, String> BINOPS = new HashMap<String, String>();
    static {
        BINOPS.put("=", "$eq");
    }
    
    StringBuilder header = new StringBuilder();
    StringBuilder statements = new StringBuilder();
    int labelCounter = 0;
    
    private String visitChild(Node node, int index) {
        return node.jjtGetChild(index).jjtAccept(this, null);
    }
    
    private String makeLabel(String prefix) {
        labelCounter ++;
        return prefix + labelCounter;
    }
    
    @Override
    public String visit(SimpleNode node, Object data) {
        throw new Error("Should not be called");
    }
    
    @Override
    public String visit(ASTStart node, Object data) {
        header.append("# Automatically created on " + new Date() + "\n");
        node.childrenAccept(this, data);
        return null;
    }
    
    @Override
    public String visit(ASTUsesDeclaration node, Object data) {
        header.append("include \"" + node.jjtGetValue() + "\"\n");
        return null;
    }
    
    @Override
    public String visit(ASTAlgo node, Object data) {
        String id = visitChild(node, 0);
        statements.append("program " + id + "\n");
        node.childrenAccept(this, data);
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
                    header.append("function " + val + " " + string  + " assignable\n");
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
        int count = node.jjtGetNumChildren();
        StringBuilder ret = new StringBuilder();
        
        for (int i = 0; i < count; i++) {
            ret.append("   ").
                append(visitChild(node, i)).
                append("\n");
        }
        
        return ret.toString();
    }
    
    @Override
    public String visit(ASTType node, Object data) {
        StringBuilder ret = new StringBuilder();
        ret.append(node.jjtGetValue());
        if(node.jjtGetNumChildren() > 1) {
            ret.append("(");
            for(int i=0; i < node.jjtGetNumChildren(); i++) {
                if(i > 0) {
                    ret.append(",");
                }
                ret.append(visitChild(node, i));
            }
            ret.append(")");
        }
        return null;
    }
    
    @Override
    public String visit(ASTAssignmentStatement node, Object data) {
        return visitChild(node, 0) + " := " + visitChild(node, 1);
    }
    
    @Override
    public String visit(ASTChooseStatement node, Object data) {
        String id = visitChild(node, 0);
        String phi = visitChild(node,1);
        return "havoc " + id + "   assume " + phi;
    }
    
    @Override
    public String visit(ASTWhileStatement node, Object data) {
        String condition = visitChild(node, 0);
        String invariant = visitChild(node, 1);

        String loopLabel = makeLabel("then");
        String bodyLabel = makeLabel("else");
        String afterLabel = makeLabel("after");

        StringBuilder ret = new StringBuilder();
        ret.append(loopLabel).append(": goto ").append(bodyLabel).append(" ")
                .append(afterLabel)
                .append("\n ").append(bodyLabel).append(": assume ").append(
                        condition)
                .append("\n").append(visitChild(node, 2)).append("  goto ")
                .append(loopLabel);
        ret.append("\n ").append(afterLabel).append(": assume $not(").append(
                condition).append(")");
        return ret.toString();
    }
    
    @Override
    public String visit(ASTIterateStatement node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    
    @Override
    public String visit(ASTIfStatement node, Object data) {
        String condition = visitChild(node, 0);

        String thenLabel = makeLabel("then");
        String elseLabel = makeLabel("else");
        String afterLabel = makeLabel("after");
        
        StringBuilder ret = new StringBuilder();
        ret.append("goto ").append(thenLabel).append(" ").append(elseLabel)
                .append("\n ").append(thenLabel).append(": assume ").append(
                        condition)
                .append("\n").append(visitChild(node, 1));
        ret.append("  goto ").append(afterLabel).append("\n ")
                  .append(elseLabel).append(": assume $not(").append(condition)
                    .append(")\n");
        if (node.jjtGetNumChildren() > 2) {
            ret.append(visitChild(node, 2));
        }
        ret.append(" ").append(afterLabel).append(":");

        return ret.toString();
    }
    
    @Override
    public String visit(ASTAssertStatement node, Object data) {
        return "assert " + visitChild(node, 0);
    }
    
    @Override
    public String visit(ASTEqual node, Object data) {
        return "$eq(" + visitChild(node, 0) + ", " + visitChild(node, 1);
    }
    @Override
    public String visit(ASTImplication node, Object data) {
        return "$imp(" + visitChild(node, 0) + ", " + visitChild(node, 1);
    }
    @Override
    public String visit(ASTDisjunction node, Object data) {
        return "$or(" + visitChild(node, 0) + ", " + visitChild(node, 1);
    }
    @Override
    public String visit(ASTConjunction node, Object data) {
        return "$and(" + visitChild(node, 0) + ", " + visitChild(node, 1);
    }
    @Override
    public String visit(ASTRelational node, Object data) {
        return "$eq(" + visitChild(node, 0) + ", " + visitChild(node, 1);
    }
    @Override
    public String visit(ASTAdd node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    @Override
    public String visit(ASTMult node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    @Override
    public String visit(ASTSetCompr node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    @Override
    public String visit(ASTSetExt node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    @Override
    public String visit(ASTSequence node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    @Override
    public String visit(ASTApplication node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    @Override
    public String visit(ASTIdentifier node, Object data) {
        return (String) node.jjtGetValue();
    }
    @Override
    public String visit(ASTInteger node, Object data) {
        // TODO Implement AlgoParserVisitor.visit
        return null;
    }
    

  
}
