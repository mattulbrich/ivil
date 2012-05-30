/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.util;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ast.AssertionStatement;
import de.uka.iti.pseudo.parser.boogie.ast.AssignmentStatement;
import de.uka.iti.pseudo.parser.boogie.ast.AssumptionStatement;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.AttributeParameter;
import de.uka.iti.pseudo.parser.boogie.ast.AxiomDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.BreakStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeBlock;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpressionReturn;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;
import de.uka.iti.pseudo.parser.boogie.ast.ConstantDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsParent;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GlobalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GotoStatement;
import de.uka.iti.pseudo.parser.boogie.ast.HavocStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfStatement;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.LocalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.Postcondition;
import de.uka.iti.pseudo.parser.boogie.ast.Precondition;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.ReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.WhileStatement;
import de.uka.iti.pseudo.parser.boogie.ast.expression.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BinaryIntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ExtendsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.expression.RelationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeApplication;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeParameter;
import de.uka.iti.pseudo.parser.boogie.ast.type.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.type.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.type.UserDefinedTypeDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.type.UserTypeDefinition;

public abstract class DefaultASTVisitor implements ASTVisitor {
    protected abstract void defaultAction(ASTElement node) throws ASTVisitException;

    @Override
    public void visit(CompilationUnit node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(AxiomDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ConstantDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(GlobalVariableDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(UserDefinedTypeDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ASTTypeApplication node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(Precondition node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ModifiesClause node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(Postcondition node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(LocalVariableDeclaration node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ProcedureBody node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(GotoStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(LoopInvariant node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(AssertionStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(AssumptionStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(HavocStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(CallStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(BinaryIntegerExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(RelationExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ExtendsExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(IntegerExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(TrueExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(FalseExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(BitvectorLiteralExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(VariableUsageExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(OldExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(LambdaExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(IfThenElseExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(AttributeParameter node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(Trigger node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(CoercionExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ExtendsParent node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(CodeBlock node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(CodeExpressionReturn node) throws ASTVisitException {
        defaultAction(node);
    }

    @Override
    public void visit(ASTTypeParameter node) throws ASTVisitException {
        defaultAction(node);
    }
}