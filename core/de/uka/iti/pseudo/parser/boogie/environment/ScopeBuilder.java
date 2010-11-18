package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.Stack;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ParseException;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * builds scope information when constructed
 * 
 * @author timm.felden@felden.com
 */
public final class ScopeBuilder extends DefaultASTVisitor {

    private final Scope globalScope;
    private final Decoration<Scope> scopeMap;

    private Stack<Scope> scopeStack;

    public ScopeBuilder(CompilationUnit root, Scope globalScope, Decoration<Scope> scopeMap)
            throws ASTVisitException {
        this.globalScope = globalScope;
        this.scopeMap = scopeMap;

        /**
         * this state variable is used to easily add block declarations to the
         * global scope and children to a nested scope. All functions that
         * change the active scope have to restore it afterwards.
         */
        scopeStack = new Stack<Scope>();
        scopeStack.push(globalScope);

        visit(root);
    }

    private void push(ASTElement node) {
        scopeStack.push(new Scope(scopeStack.peek(), node));
    }

    private void pop(ASTElement node) {
        // if this assertion fails you most probably forgot to pop somewhere
        assert scopeStack.peek().creator == node;
        scopeStack.pop();
        assert scopeStack.size() > 0;
    }

    private Scope activeScope() {
        return scopeStack.peek();
    }

    /**
     * The usual behavior is to add the element to the parents scope.
     * 
     * @throws ASTVisitException
     *             no exception should be thrown here unless something strange
     *             happened
     */
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        if (scopeMap.has(node))
            return; // this can happen in variable declarations of type
                    // "const A,B:bool;"

        scopeMap.add(node, activeScope());

        for (ASTElement n : node.getChildren())
            n.visit(this);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        scopeMap.add(node, globalScope);

        push(node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        pop(node);
    }

    /**
     * MapTypes lie in their own scope to handle type parameters more easily.
     * For example if we have some Type S; and a Type T = <S>[[S] int] int, the
     * definition is equal to Type T = <_> [[_] int] int.<br>
     * The restriction, that _ has to be mentioned somewhere in the domain,
     * should not be a concern here.
     */
    @Override
    public void visit(MapType node) throws ASTVisitException {
        scopeMap.add(node, activeScope());

        push(node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        pop(node);
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        scopeMap.add(node, globalScope);

        push(node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        pop(node);
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        scopeMap.add(node, globalScope);

        push(node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        pop(node);
    }

    @Override
    public void visit(Variable node) throws ASTVisitException {
        scopeMap.add(node, activeScope());

        push(node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        pop(node);
    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {
        scopeMap.add(node, activeScope());

        push(node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        pop(node);
    }


}
