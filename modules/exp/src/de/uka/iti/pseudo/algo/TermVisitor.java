package de.uka.iti.pseudo.algo;

public class TermVisitor extends DefaultAlgoParserVisitor {

    private final Translation translation;

    /**
     * @param translation
     */
    public TermVisitor(Translation translation) {
        super();
        this.translation = translation;
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visit(ASTBinderIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visit(ASTAbbrevIdentifier node, Object data) {
        return translation.getAbbreviatedTerm(node.jjtGetValue());
    }

    @Override
    public String visit(ASTSetExtensionExpression node, Object data) {
        if(node.jjtGetNumChildren() > 0) {
            return "singleton(" + visitChild(node, 0) + ")";
        } else {
            return "emptyset";
        }
    }

    @Override
    public String visit(ASTSetComprehensionExpression node, Object data) {
        String var = visitChild(node, 0);
        String cond = visitChild(node, 1);
        return "(\\set " + var + " ; " + cond + ")";
    }

    @Override
    public String visit(ASTMapAccessExpression node, Object data) {
        // return "$load(" + visitChild(node, 0) + ", " + visitChild(node, 1) + ")";
        return visitChild(node, 0) + "[" + visitChild(node, 1) + "]";
    }

    @Override
    public String visit(ASTBinaryExpression node, Object data) {
        assert node.jjtGetNumChildren() == 2;
        return "(" + visitChild(node, 0) + " " +
                node.jjtGetValue().toString() + " " +
                visitChild(node, 1) + ")";
    }

    @Override
    public String visit(ASTUnaryExpression node, Object data) {
        assert node.jjtGetNumChildren() == 1;
        return node.jjtGetValue().toString() + "("
                + visitChild(node, 0) + ")";
    }

    @Override
    public String visit(ASTApplicationExpression node, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append(visitChild(node, 0));
        if(node.jjtGetNumChildren() > 1) {
            sb.append("(");
            sb.append(visitChild(node, 1));
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public String visit(ASTFieldAccessExpression node, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append(visitChild(node, 0));
        sb.append("[");
        sb.append(visitChild(node, 1));
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String visit(ASTBinderExpression node, Object data) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(visitChild(node, 0));
        sb.append(" ");
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            if(i != 1) {
                sb.append("; ");
            }
            sb.append(visitChild(node, i));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visit(ASTExpressionCommaList node, Object data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if(i != 0) {
                sb.append(", ");
            }
            sb.append(visitChild(node, i));
        }
        return sb.toString();
    }

    @Override
    public String visit(ASTAsExpression node, Object data) {
        return "((" + visitChild(node, 0) + ") as " +
                visitChild(node, 1) + ")";
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
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("TermVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

}
