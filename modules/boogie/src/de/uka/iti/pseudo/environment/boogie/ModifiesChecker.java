package de.uka.iti.pseudo.environment.boogie;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.LocalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.Specification;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

public final class ModifiesChecker extends DefaultASTVisitor {

    private final EnvironmentCreationState state;
    private final LinkedList<VariableDeclaration> locallyModifiable = new LinkedList<VariableDeclaration>();
    private List<VariableDeclaration> modifiable = null;
    private static final List<VariableDeclaration> empty = new LinkedList<VariableDeclaration>();

    private final LinkedList<ASTElement> todo = new LinkedList<ASTElement>();

    /**
     * this is true iff we are in a code expression. In code expressions no
     * global variables are modifiable.
     */
    private boolean inCodeexpression = false;

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    public ModifiesChecker(EnvironmentCreationState state) throws TypeSystemException {
        this.state = state;

        try {
            state.root.visit(this);
        } catch (ASTVisitException e) {
            e.printStackTrace();
            throw new TypeSystemException("modifies not respected\n" + e);
        }
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        modifiable = new LinkedList<VariableDeclaration>();
        locallyModifiable.clear();

        locallyModifiable.addAll(node.getOutParameters());

        for (Specification spec : node.getSpecification())
            spec.visit(this);

        if (node.isImplemented())
            node.getBody().visit(this);

        state.types.modifiable.put(node, modifiable);
    }

    @Override
    public void visit(ModifiesClause node) throws ASTVisitException {

        for (String name : node.getTargets()) {
            VariableDeclaration v = state.names.findVariable(name, node);
            if (null == v)
                throw new ASTVisitException(node.getLocation() + " modification of undeclared variable " + name);

            if (!state.scopeMap.get(v).equals(state.globalScope))
                throw new ASTVisitException(node.getLocation() + " only global variables may be modified");

            modifiable.add(v);
        }
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        if (!state.types.modifiable.containsKey(state.names.procedureSpace.get(node.getName())))
            todo.add(node);

        modifiable = state.types.modifiable.get(state.names.procedureSpace.get(node.getName()));
        locallyModifiable.clear();
        locallyModifiable.addAll(node.getOutParameters());

        defaultAction(node);
    }

    @Override
    public void visit(LocalVariableDeclaration node) throws ASTVisitException {
        locallyModifiable.addAll(node.getVariableDeclarations());
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        // can contain codeexpressions with assignments
        node.getNewValue().visit(this);

        VariableUsageExpression lvalue;
        if (node.getTarget() instanceof VariableUsageExpression) {
            lvalue = (VariableUsageExpression) node.getTarget();
        } else {
            Expression target = node.getTarget();
            while (!(target instanceof VariableUsageExpression)) {
                target = ((MapAccessExpression) target).getName();
            }
            lvalue = (VariableUsageExpression) target;
        }

        assert locallyModifiable != null : "locally modifiable variables are null?!?";
        assert modifiable != null : "globally modifiable variables are null?!?";

        VariableDeclaration v = state.names.findVariable(lvalue);
        if (locallyModifiable.contains(v) || modifiable.contains(v))
            return;

        throw new ASTVisitException("@" + node.getLocation() + ": modified unmodifiable variable " + v);
    }

    public void visit(CallStatement node) throws ASTVisitException {
        defaultAction(node);

        // check modifies clauses of called procedure
        ProcedureDeclaration decl = state.names.procedureSpace.get(node.getName());
        for (Specification s : decl.getSpecification()) {
            if (s instanceof ModifiesClause) {
                for (String name : ((ModifiesClause) s).getTargets()) {
                    VariableDeclaration v = state.names.findVariable(name, node);

                    if (!(locallyModifiable.contains(v) || modifiable.contains(v)))
                        throw new ASTVisitException("@" + node.getLocation()
                                + ": call may modify unmodifiable variable " + v);
                }
            }
        }
    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        if (inCodeexpression) {
            defaultAction(node);
        } else {
            inCodeexpression = true;
            List<VariableDeclaration> mod = modifiable, modl = new LinkedList<VariableDeclaration>(locallyModifiable);
            modifiable = empty;
            locallyModifiable.clear();

            defaultAction(node);

            locallyModifiable.clear();
            locallyModifiable.addAll(modl);
            modifiable = mod;
            inCodeexpression = false;
        }
    }
}
