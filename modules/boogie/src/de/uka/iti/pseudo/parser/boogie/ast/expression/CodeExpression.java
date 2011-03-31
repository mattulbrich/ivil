package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.CodeBlock;
import de.uka.iti.pseudo.parser.boogie.ast.LocalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.NamedASTElement;

public final class CodeExpression extends Expression implements NamedASTElement {

    final private List<LocalVariableDeclaration> vars;
    final private List<CodeBlock> blocks;
    final private List<Expression> operands = new LinkedList<Expression>();

    public CodeExpression(Token location, List<LocalVariableDeclaration> vars, List<CodeBlock> blocks)
            throws ParseException {
        super(location);

        this.blocks = blocks;
        this.vars = vars;

        addChildren(vars);
        addChildren(blocks);
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<CodeBlock> getCode() {
        return blocks;
    }

    @Override
    public String getName() {
        return "|{" + getLocation().replace(":", "_") + "}|";
    }

    public List<LocalVariableDeclaration> getVars() {
        return vars;
    }

}
