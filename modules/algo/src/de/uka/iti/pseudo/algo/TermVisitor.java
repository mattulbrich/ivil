package de.uka.iti.pseudo.algo;

import java.util.List;

public class TermVisitor extends DefaultAlgoParserVisitor {

    private final Translation translation;
    private final String mapFunction;
    private final List<String> statements;

    /**
     * The statement list will be used to add assertions.
     *
     * @param translation
     *            the translation which is in use
     * @param statements
     *            the statement buffer containg the translation.
     */
    public TermVisitor(Translation translation, List<String> statements) {
        super();
        this.translation = translation;
        this.statements = statements;
        this.mapFunction = translation.getOption("mapFunction");
    }

    public TermVisitor(Translation translation) {
        this(translation, null);
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
        String map = visitChild(node, 0);
        String index = visitChild(node, 1);
        if(mapFunction == null) {
            return map + "[" + index + "]";
        } else {
            if (statements != null && "seqGet".equals(mapFunction)) {
                // XXX find a more flexible solution here
//                statements.add("  assert 0 <= (" + index + ") & (" +
//                        index + ") < seqLen(" + map + ") ; \"sequence index in range\"");
            }
            return mapFunction + "(" + map + ", " + index + ")";
        }
    }

    @Override
    public String visit(ASTBinaryExpression node, Object data) {
        assert node.jjtGetNumChildren() == 2;
        String operator = node.jjtGetValue().toString();

        if (statements != null && "/".equals(operator)) {
            statements.add("  assert !" + visitChild(node, 1) +
                    " = 0 ; \"check denominator not zero\"");
        }

        return "(" + visitChild(node, 0) + " " + operator + " " +
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
        String name = visitChild(node, 0);

        if(statements != null && "card".equals(name)) {
            statements.add("  assert finite(" + visitChild(node, 1) + ")");
        }

        sb.append(name);
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
