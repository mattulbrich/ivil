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
import de.uka.iti.pseudo.parser.boogie.ast.CodeBlock;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpressionReturn;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CompilationUnit;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConstantDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsParent;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GlobalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GotoStatement;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.HavocStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
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
import de.uka.iti.pseudo.parser.boogie.ast.Postcondition;
import de.uka.iti.pseudo.parser.boogie.ast.Precondition;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.ReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.TemplateType;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UserDefinedTypeDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WhileStatement;
import de.uka.iti.pseudo.parser.boogie.ast.WildcardExpression;

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

    void visit(TemplateType node) throws ASTVisitException;

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
}
