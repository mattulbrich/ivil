package de.uka.iti.pseudo.parser.boogie.environment;

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
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BreakStatement;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConstantDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GlobalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GotoStatement;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.HavocStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LocalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecParent;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecification;
import de.uka.iti.pseudo.parser.boogie.ast.PartialLessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Postcondition;
import de.uka.iti.pseudo.parser.boogie.ast.Precondition;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.ReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SpecBlock;
import de.uka.iti.pseudo.parser.boogie.ast.SpecReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.TemplateType;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UserDefinedTypeDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WhileStatement;
import de.uka.iti.pseudo.parser.boogie.ast.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * translates universal types to ivil types
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypeTranslator extends DefaultASTVisitor {
    private final EnvironmentCreationState state;

    public TypeTranslator(EnvironmentCreationState state) throws ASTVisitException {
        this.state = state;

        state.root.visit(this);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        if (null != state.typeMap.get(node)) {
            state.ivilTypeMap.add(node, null);
        } else
            state.ivilTypeMap.add(node, null);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AxiomDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConstantDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Variable node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GlobalVariableDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(UserDefinedTypeDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(TemplateType node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Precondition node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ModifiesClause node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Postcondition node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LocalVariableDeclaration node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ProcedureBody node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GotoStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IfStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LoopInvariant node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AssertionStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AssumptionStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(HavocStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CallStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AdditionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SubtractionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(EqualsNotExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LessExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LessThenExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GreaterExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GreaterThenExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(PartialLessExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MultiplicationExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(DivisionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ModuloExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IntegerExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(TrueExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(FalseExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BitvectorLiteralExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(VariableUsageExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OldExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LambdaExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IfThenElseExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AttributeParameter node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Trigger node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CoercionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OrderSpecParent node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OrderSpecification node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SpecBlock node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SpecReturnStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

}
