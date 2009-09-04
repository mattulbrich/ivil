package de.uka.iti.pseudo.comp.rascal;

import java.util.List;

public class Translator extends DefaultVisitor {

    private StatementRecorder statements = new StatementRecorder();

    private Environment env;

    private int targetCounter = 0;

    private RegisterBank registerBank;

    public Translator(Environment env) {
        super();
        this.env = env;
        this.registerBank = new RegisterBank(env);
    }

    @Override protected Object visitDefault(Node node, Object arg) {
        throw new IllegalStateException("should not be called for " + node);
    }

    //
    // PROC DECL
    //

    @Override public Object visit(ASTProcDecl node, Object arg) {

        StatementRecorder finalChecks = new StatementRecorder();

        addInvariants(statements, "assume ");
        addInvariants(finalChecks, "assert ");

        String name = node.jjtGetChild(0).getImage();
        Procedure contract = env.contractMap.get(name);

        Token pre = contract.getPrecondition();
        if (pre != null) {
            statements.add(pre.beginLine, "assume " + pre.image);
        }

        Token post = contract.getPostcondition();
        if (post != null) {
            finalChecks.add(post.beginLine, "assert " + post.image);
        }

        Token modifies = contract.getModifies();
        if (modifies != null) {
            finalChecks.add(post.beginLine, createModifiesObligation(modifies));
        } else {
            finalChecks.switchOffLine();
            finalChecks.add(createModifiesObligation(null));
        }

        node.getFirstChild(ASTStatementSeq.class).childrenAccept(this, null);

        statements.addLabel("procEnd");
        statements.addAll(finalChecks);

        return null;
    }

    private String createModifiesObligation(Token modList) {
        // TODO later with quantified types. ... when all this is defined
        return "skip";
    }

    private void addInvariants(StatementRecorder list, String keyword) {
        for (Token inv : env.invariants) {
            list.add(inv.beginLine, keyword + inv.image);
        }
    }

    //
    // ------ Statements
    //

    //
    // IF

    @Override public Object visit(ASTIfStmt node, Object arg) {

        statements.setLineOf(node);

        TokenNode expr = node.jjtGetChild(0);
        ASTStatementSeq thenSeq = (ASTStatementSeq) node.jjtGetChild(1);
        ASTStatementSeq elseSeq = (ASTStatementSeq) node.jjtGetChild(2);

        String exprString = (String) expr.jjtAccept(this, null);

        String thenTarget = makeTarget("then");
        String elseTarget = makeTarget("else");
        String afterTarget = makeTarget("after");

        statements.add("$cnd := " + exprString);
        statements.add("goto " + thenTarget + " " + elseTarget);

        statements.addLabel(thenTarget);
        statements.add("assume $cnd");
        thenSeq.childrenAccept(this, null);
        statements.add("goto " + afterTarget);

        statements.setLineOf(node);
        statements.addLabel(elseTarget);
        statements.add("assume !$cnd");
        if (elseSeq != null)
            elseSeq.childrenAccept(this, null);

        statements.addLabel(afterTarget);
        return null;
    }

    //
    // WHILE

    @Override public Object visit(ASTWhileStmt node, Object arg) {

        TokenNode expr = node.jjtGetChild(0);
        ASTStatementSeq bodySeq = (ASTStatementSeq) node.jjtGetChild(1);

        String loopTarget = makeTarget("loop");
        String bodyTarget = makeTarget("body");
        String afterTarget = makeTarget("after");

        statements.setLineOf(node);
        statements.addLabel(loopTarget);
        String exprString = (String) expr.jjtAccept(this, null);
        statements.add("$cnd := " + exprString);

        statements.add("goto " + bodyTarget + " " + afterTarget);

        statements.addLabel(bodyTarget);
        statements.add("assume $cnd");
        bodySeq.childrenAccept(this, null);
        statements.add("goto " + loopTarget);

        statements.setLineOf(node);
        statements.addLabel(afterTarget);
        statements.add("assume !$cnd");
        return null;
    }

    //
    // RETURN

    @Override public Object visit(ASTReturnStmt node, Object arg) {

        TokenNode expr = node.jjtGetChild(0);

        statements.setLineOf(node);
        String exprString = (String) expr.jjtAccept(this, null);
        statements.add("$result := " + exprString);
        statements.add("goto procEnd");

        return null;
    }

    //
    // METHOD CALL STATEMENTS

    @Override public Object visit(ASTProcCallStmt node, Object arg) {
        String name = node.jjtGetChild(0).getImage();

        statements.setLineOf(node);

        Procedure procedure = env.contractMap.get(name);
        List<Pair<String, Type>> parameters = procedure.getParameters();
        int i = 1;
        for (Pair<String, Type> param : parameters) {
            String reg = registerBank
                    .getNewRegister(param.snd().toSimpleType());
            String exprString = (String) node.jjtGetChild(i).jjtAccept(this,
                    null);
            statements.add(reg + " := " + exprString);
        }

        // TODO replace params by newly assigned registers
        statements.add(procedure.getPrecondition().beginLine, "assert true");
        statements.add("havoc h");
        statements.add(procedure.getPostcondition().beginLine, "assume true");
        // assume \forall o; \forall f; sel($h,o,f) = sel($h1,o,f) |
        // mem(loc(o,f), modset)
        statements.add(procedure.getModifies().beginLine, "assume true");

        return null;
    }

    // 
    // ASSIGNMENT
    @Override public Object visit(ASTAssignmentStmt node, Object arg) {
        statements.setLineOf(node);

        TokenNode target = node.jjtGetChild(0);
        TokenNode value = node.jjtGetChild(1);
        String valueString = (String) value.jjtAccept(this, null);

        if (target instanceof ASTIdentifier) {
            String name = target.getImage();
            statements.add(name + " := " + valueString);
        } else

        if (target instanceof ASTArrayDesig) {
            String indexString = (String) target.jjtGetChild(1).jjtAccept(this,
                    null);
            String prefixString = (String) target.jjtGetChild(0).jjtAccept(
                    this, null);
            statements.add("$h := storA($h, " + prefixString + ", "
                    + indexString + ", " + valueString + ")");
        } else

        if (target instanceof ASTFieldDesig) {
            String field = target.jjtGetChild(1).getImage();
            String prefixString = (String) target.jjtGetChild(0).jjtAccept(
                    this, null);
            statements.add("$h := stor($h, " + prefixString + ", " + field
                    + ", " + valueString + ")");
        }

        return null;
    }

    //
    // ------ Expressions
    // they return their representation string!

    //
    // Literals (TRUE --> true)

    @Override public Object visit(ASTLiteral node, Object arg) {
        return node.getImage().toLowerCase();
    }

    //
    // Identifiers
    @Override public Object visit(ASTIdentifier node, Object arg) {
        return node.getImage();
    }

    //
    // Operations
    @Override public Object visit(ASTArithmetic node, Object arg) {
        String first = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String operator = node.jjtGetChild(1).getImage();
        String second = (String) node.jjtGetChild(2).jjtAccept(this, null);

        return "(" + first + " " + operator + " " + second + ")";
    }

    //
    // Negative prefix
    @Override public Object visit(ASTNegativeExpr node, Object arg) {
        String operand = (String) node.jjtGetChild(0).jjtAccept(this, null);
        return "-(" + operand + ")";
    }

    //
    // Negation prefix !
    @Override public Object visit(ASTNegation node, Object arg) {
        String operand = (String) node.jjtGetChild(0).jjtAccept(this, null);
        return "!(" + operand + ")";
    }

    //
    // Field access term.field
    @Override public Object visit(ASTFieldDesig node, Object arg) {
        String operand = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String field = node.jjtGetChild(1).getImage();
        statements.add("assert !nil = " + operand);
        return "sel($h, " + operand + ", " + field + ")";
    }

    //
    // Array access term[index]
    @Override public Object visit(ASTArrayDesig node, Object arg) {
        String operand = (String) node.jjtGetChild(0).jjtAccept(this, null);
        String index = (String) node.jjtGetChild(1).jjtAccept(this, null);
        statements.add("assert 0 < " + index + " & " + index + " <= sel($h,"
                + operand + ", length)");
        return "selA($h, " + operand + ", " + index + ")";
    }

    @Override public Object visit(ASTProcCall node, Object arg) {
        // TODO Implement Translator.visit
        String name = node.jjtGetChild(0).getImage();

        statements.setLineOf(node);

        Procedure procedure = env.contractMap.get(name);
        List<Pair<String, Type>> parameters = procedure.getParameters();
        int i = 1;
        for (Pair<String, Type> param : parameters) {
            String reg = registerBank
                    .getNewRegister(param.snd().toSimpleType());
            String exprString = (String) node.jjtGetChild(i).jjtAccept(this,
                    null);
            statements.add(reg + " := " + exprString);
        }
        
        String resultRegister = registerBank.getNewRegister(procedure.getReturnType().toSimpleType());
        statements.add("havoc " + resultRegister);

        // TODO replace params by newly assigned registers
        statements.add(procedure.getPrecondition().beginLine, "assert true");
        statements.add("havoc h");
        statements.add(procedure.getPostcondition().beginLine, "assume true");
        // assume \forall o; \forall f; sel($h,o,f) = sel($h1,o,f) |
        // mem(loc(o,f), modset)
        statements.add(procedure.getModifies().beginLine, "assume true");

        return resultRegister;
    }

    //
    // HELPER
    //

    private String makeTarget(String prefix) {
        return prefix + (targetCounter++);
    }

    public RegisterBank getRegisterBank() {
        return registerBank;
    }

    public List<String> getStatements() {
        return statements.getStrings();
    }
}
