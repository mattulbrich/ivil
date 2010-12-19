package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.LocalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SpecBlock;
import de.uka.iti.pseudo.parser.boogie.ast.Specification;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

public final class ModifiesChecker extends DefaultASTVisitor {

    private final EnvironmentCreationState state;
    private final LinkedList<Variable> locallyModifiable = new LinkedList<Variable>();
    private List<Variable> modifiable = null;

    private final LinkedList<ASTElement> todo = new LinkedList<ASTElement>();

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
        modifiable = new LinkedList<Variable>();
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
            Variable v = state.names.findVariable(name, node);
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
        locallyModifiable.addAll(node.getVariables());
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
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

        Variable v = state.names.findVariable(lvalue);
        if (locallyModifiable.contains(v) || modifiable.contains(v))
            return;

        throw new ASTVisitException("@" + node.getLocation() + ": modified unmodifiable variable " + v);
    }
}
