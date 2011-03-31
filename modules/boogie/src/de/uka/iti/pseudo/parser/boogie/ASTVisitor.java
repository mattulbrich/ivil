/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (node) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this node) for details.
 */
package de.uka.iti.pseudo.parser.boogie;

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
import de.uka.iti.pseudo.parser.boogie.ast.expression.AdditionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ExtendsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.GreaterEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.LessEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.expression.SubtractionExpression;
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

/**
 * A visitor interface for a boogie AST.
 * 
 * @author timm.felden@felden.com
 */
public interface ASTVisitor {

    void visit(CompilationUnit node) throws ASTVisitException;

    void visit(AxiomDeclaration node) throws ASTVisitException;

    void visit(ConstantDeclaration node) throws ASTVisitException;

    void visit(VariableDeclaration node) throws ASTVisitException;

    void visit(FunctionDeclaration node) throws ASTVisitException;

    void visit(GlobalVariableDeclaration node) throws ASTVisitException;

    void visit(BuiltInType node) throws ASTVisitException;

    void visit(UserTypeDefinition node) throws ASTVisitException;

    void visit(UserDefinedTypeDeclaration node) throws ASTVisitException;

    void visit(ASTTypeApplication node) throws ASTVisitException;

    void visit(MapType node) throws ASTVisitException;

    void visit(ProcedureDeclaration node) throws ASTVisitException;

    void visit(Precondition node) throws ASTVisitException;

    void visit(ModifiesClause node) throws ASTVisitException;

    void visit(Postcondition node) throws ASTVisitException;

    void visit(ProcedureImplementation node) throws ASTVisitException;

    void visit(LocalVariableDeclaration node) throws ASTVisitException;

    void visit(ProcedureBody node) throws ASTVisitException;

    void visit(GotoStatement node) throws ASTVisitException;

    void visit(ReturnStatement node) throws ASTVisitException;

    void visit(IfStatement node) throws ASTVisitException;

    void visit(LoopInvariant node) throws ASTVisitException;

    void visit(WhileStatement node) throws ASTVisitException;

    void visit(BreakStatement node) throws ASTVisitException;

    void visit(AssertionStatement node) throws ASTVisitException;

    void visit(AssumptionStatement node) throws ASTVisitException;

    void visit(HavocStatement node) throws ASTVisitException;

    void visit(WildcardExpression node) throws ASTVisitException;

    void visit(CallForallStatement node) throws ASTVisitException;

    void visit(CallStatement node) throws ASTVisitException;

    void visit(LabelStatement node) throws ASTVisitException;

    void visit(SimpleAssignment node) throws ASTVisitException;

    void visit(AssignmentStatement node) throws ASTVisitException;

    void visit(AdditionExpression node) throws ASTVisitException;

    void visit(SubtractionExpression node) throws ASTVisitException;

    void visit(EquivalenceExpression node) throws ASTVisitException;

    void visit(ImpliesExpression node) throws ASTVisitException;

    void visit(AndExpression node) throws ASTVisitException;

    void visit(OrExpression node) throws ASTVisitException;

    void visit(EqualsExpression node) throws ASTVisitException;

    void visit(EqualsNotExpression node) throws ASTVisitException;

    void visit(LessExpression node) throws ASTVisitException;

    void visit(LessEqualExpression node) throws ASTVisitException;

    void visit(GreaterExpression node) throws ASTVisitException;

    void visit(GreaterEqualExpression node) throws ASTVisitException;

    void visit(ExtendsExpression node) throws ASTVisitException;

    void visit(ConcatenationExpression node) throws ASTVisitException;

    void visit(MultiplicationExpression node) throws ASTVisitException;

    void visit(DivisionExpression node) throws ASTVisitException;

    void visit(ModuloExpression node) throws ASTVisitException;

    void visit(UnaryMinusExpression node) throws ASTVisitException;

    void visit(NegationExpression node) throws ASTVisitException;

    void visit(IntegerExpression node) throws ASTVisitException;

    void visit(BitvectorSelectExpression node) throws ASTVisitException;

    void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException;

    void visit(MapAccessExpression node) throws ASTVisitException;

    void visit(TrueExpression node) throws ASTVisitException;

    void visit(FalseExpression node) throws ASTVisitException;

    void visit(BitvectorLiteralExpression node) throws ASTVisitException;

    void visit(FunctionCallExpression node) throws ASTVisitException;

    void visit(VariableUsageExpression node) throws ASTVisitException;

    void visit(OldExpression node) throws ASTVisitException;

    void visit(QuantifierBody node) throws ASTVisitException;

    void visit(ForallExpression node) throws ASTVisitException;

    void visit(ExistsExpression node) throws ASTVisitException;

    void visit(LambdaExpression node) throws ASTVisitException;

    void visit(IfThenElseExpression node) throws ASTVisitException;

    void visit(AttributeParameter node) throws ASTVisitException;

    void visit(Attribute node) throws ASTVisitException;

    void visit(Trigger node) throws ASTVisitException;

    void visit(CoercionExpression node) throws ASTVisitException;

    void visit(ExtendsParent node) throws ASTVisitException;

    void visit(MapUpdateExpression node) throws ASTVisitException;

    void visit(CodeBlock node) throws ASTVisitException;

    void visit(CodeExpression node) throws ASTVisitException;

    void visit(CodeExpressionReturn node) throws ASTVisitException;

    void visit(ASTTypeParameter node) throws ASTVisitException;
}
