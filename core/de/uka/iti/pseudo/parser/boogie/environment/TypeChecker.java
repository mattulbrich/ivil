package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.AdditionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AssertionStatement;
import de.uka.iti.pseudo.parser.boogie.ast.AssignmentStatement;
import de.uka.iti.pseudo.parser.boogie.ast.AssumptionStatement;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.AttributeParameter;
import de.uka.iti.pseudo.parser.boogie.ast.AxiomDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BreakStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.HavocStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecParent;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecification;
import de.uka.iti.pseudo.parser.boogie.ast.PartialLessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Postcondition;
import de.uka.iti.pseudo.parser.boogie.ast.Precondition;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SpecBlock;
import de.uka.iti.pseudo.parser.boogie.ast.SpecReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.TemplateType;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WhileStatement;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

public final class TypeChecker extends DefaultASTVisitor {

    private final EnvironmentCreationState state;
    private final List<String> errorMessages = new LinkedList<String>();

    public TypeChecker(EnvironmentCreationState environmentCreationState) throws TypeSystemException {
        this.state = environmentCreationState;

        try {
            visit(state.root);
        } catch (ASTVisitException e) {
            e.printStackTrace();

            error("an unexpected ASTVisitException was thrown", state.root);
        }

        if (errorMessages.size() != 0) {
            StringBuffer msg = new StringBuffer();
            msg.append(errorMessages.size());
            msg.append(" errors occured:\n");
            for (String s : errorMessages)
                msg.append(s);

            throw new TypeSystemException(msg.toString());
        }
    }

    private void expect(ASTElement node, ASTElement type) {
        expect(node, state.typeMap.get(type));
    }

    /**
     * Expects node to be of type type; reports error on mismatch.
     * 
     * @param node
     *            target node
     * @param type
     *            expected type
     */
    private void expect(ASTElement node, UniversalType type) {
        if (!state.typeMap.get(node).compatible(type))
            error("expected node to be of type " + type + ", but found " + state.typeMap.get(node), node);
    }

    /**
     * Pushes a new error
     * 
     * @param message
     *            a message that will help the user to understand the error
     * 
     * @param node
     *            the node, that revealed the error
     */
    private void error(String message, ASTElement node) {
        errorMessages.add(message + "\n\tcaused by node " + node + "\n");
    }

    protected void defaultAction(ASTElement node) throws ASTVisitException {
        if (!state.typeMap.has(node))
            error("found untyped element", node); // this would indicate
                                                  // an error in scope and
                                                  // type decorations

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AxiomDeclaration node) throws ASTVisitException {
        expect(node.getAxiom(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitException {
        expect(node.getWhereClause(), UniversalType.BOOL_T);

        if (state.typeMap.get(node).isConstructor())
            error("it is not allowed to create variables of incomplete type", node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(TemplateType node) throws ASTVisitException {
        // TODO Auto-generated method stub


        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Precondition node) throws ASTVisitException {
        expect(node.getCondition(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Postcondition node) throws ASTVisitException {
        expect(node.getCondition(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureBody node) throws ASTVisitException {
        // procedure needs to be declared and of compatible type

        // TODO Auto-generated method stub
        // compatible iff paths equal and rest compatbile? free order!


        for (ASTElement e : node.getChildren())
            e.visit(this);

    }

    @Override
    public void visit(IfStatement node) throws ASTVisitException {
        expect(node.getGuard(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(LoopInvariant node) throws ASTVisitException {
        expect(node.getExpression(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitException {
        expect(node.getGuard(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitException {
        if (node.hasTarget())
            if (!state.labelSpace.containsKey(new Pair<String, Scope>(node.getTarget(), state.scopeMap.get(node))))
                error("unable to jumpt to unknown label:" + node.getTarget(), node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AssertionStatement node) throws ASTVisitException {
        expect(node.getAssertion(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AssumptionStatement node) throws ASTVisitException {
        expect(node.getAssertion(), UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(HavocStatement node) throws ASTVisitException {
        for (String name : node.getVarnames()) {
            Scope scope;
            for (scope = state.scopeMap.get(node); scope != null; scope = scope.parent)
                if (state.variableSpace.containsKey(new Pair<String, Scope>(name, scope)))
                    break;

            if (null == scope)
                error("can not havoc unknown variable " + name, node);
        }

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(CallStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AdditionExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(SubtractionExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        expect(node.getOperands().get(0), node.getOperands().get(1));

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(EqualsNotExpression node) throws ASTVisitException {
        expect(node.getOperands().get(0), node.getOperands().get(1));

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(LessExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(LessThenExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(GreaterExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(GreaterThenExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(PartialLessExpression node) throws ASTVisitException {
        expect(node.getOperands().get(0), node.getOperands().get(1));

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        try {
            for(ASTElement n : node.getOperands())
                state.typeMap.get(n).getBVDimension();
        } catch (IllegalArgumentException e) {
            error("concatenation is only allowed on bitvectors", node);
        }

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(MultiplicationExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(DivisionExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ModuloExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.INT_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        for (Expression op : node.getOperands())
            expect(op, UniversalType.BOOL_T);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        if (!state.typeMap.get(node.getBody()).range.compatible(UniversalType.newBool()))
            error("forall expects a body of type bool", node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {
        if (!state.typeMap.get(node.getBody()).range.compatible(UniversalType.newBool()))
            error("exists expects a body of type bool", node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(IfThenElseExpression node) throws ASTVisitException {

        expect(node.getCondition(), UniversalType.BOOL_T);
        expect(node.getThen(), node.getElse());

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(CoercionExpression node) throws ASTVisitException {

        expect(node.getOperands().get(0), node.getType());

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(OrderSpecParent node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(OrderSpecification node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(SpecBlock node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(SpecReturnStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

}
