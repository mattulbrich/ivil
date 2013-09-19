package de.uka.iti.pseudo.algo;

public abstract class DefaultAlgoParserVisitor implements AlgoParserVisitor {

    public abstract String visitDefault(SimpleNode node, Object data);

    protected final String visitChild(Node node, int index) {
        return node.jjtGetChild(index).jjtAccept(this, null);
    }

    @Override
    public String visit(SimpleNode node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTStart node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTUsesInputDeclaration node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTOption node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTUsesInlineDeclaration node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAlgo node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTInputDecl node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAbbreviation node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTOutputDecl node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTVarDecl node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTRequiresDecl node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTEnsuresDecl node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTStatementBlock node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTType node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAssignmentStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTMapAssignmentStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTReturnStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTChooseStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTWhileStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTIterateStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTIfStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAssertStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAssumeStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTNoteStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTMarkStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTInlineStatement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTBinderIdentifier node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAbbrevIdentifier node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTBinaryExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTUnaryExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTMapAccessExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTAsExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTFieldAccessExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTApplicationExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTBinderExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTExpressionCommaList node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTSetExtensionExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTSetComprehensionExpression node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTRefinement node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTCouplingFormula node, Object data) {
        return visitDefault(node, data);
    }

    @Override
    public String visit(ASTHint node, Object data) {
        return visitDefault(node, data);
    }
}
