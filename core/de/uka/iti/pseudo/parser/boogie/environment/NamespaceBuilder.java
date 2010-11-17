package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.AdditionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AssignmentStatement;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.AttributeParameter;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecParent;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecification;
import de.uka.iti.pseudo.parser.boogie.ast.PartialLessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * This visitor fills all *Space fields of state with information.
 * 
 * @author timm.felden@felden.com
 */
public class NamespaceBuilder extends DefaultASTVisitor {

    private final EnvironmentCreationState state;

    // used to create labelSpace
    private ProcedureBody currentBody = null;

    public NamespaceBuilder(EnvironmentCreationState environmentCreationState) throws ASTVisitException {
        state = environmentCreationState;

        visit(state.root);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitException {
        Pair<String, Scope> key = new Pair<String, Scope>(node.getName(), state.scopeMap.get(node));
        
        if (state.variableSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.variableSpace.get(key).getLocation());
        
        state.variableSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        final String key = node.getName();

        if (state.functionSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.functionSpace.get(key).getLocation());

                state.functionSpace.put(key, node);

for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        // this is used to register all actually used built in types; if we wont
        // do this, we could not insert bv-Types into the typeSpace as they
        // would create an infinite amount of entries

        state.typeSpace.put(node.getPrettyName(), node);

        // no check needed here, grammar does not allow to create harmful
        // duplicates

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        final String key = node.getName();

        if (state.typeSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.typeSpace.get(key).getLocation());

        state.typeSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        final String key = node.getName();

        if (state.procedureSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.procedureSpace.get(key).getLocation());

        state.procedureSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureBody node) throws ASTVisitException {
        currentBody = node;

        for (ASTElement e : node.getChildren())
            e.visit(this);

        currentBody = null;
    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        assert currentBody != null;
        
        Pair<String, ProcedureBody> key = new Pair<String, ProcedureBody>(node.getName(), currentBody);

        if (state.variableSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.variableSpace.get(key).getLocation());

        state.labelSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        final String key = node.getName();

        state.attributeSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }
}
