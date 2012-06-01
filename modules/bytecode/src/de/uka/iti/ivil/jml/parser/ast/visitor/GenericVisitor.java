/*
 * Copyright (C) 2007 J�lio Vilmar Gesser.
 * 
 * This file is part of Java 1.5 parser and Abstract Syntax Tree.
 *
 * Java 1.5 parser and Abstract Syntax Tree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java 1.5 parser and Abstract Syntax Tree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java 1.5 parser and Abstract Syntax Tree.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on 05/10/2006
 */
package de.uka.iti.ivil.jml.parser.ast.visitor;

import de.uka.iti.ivil.jml.parser.ast.BlockComment;
import de.uka.iti.ivil.jml.parser.ast.CompilationUnit;
import de.uka.iti.ivil.jml.parser.ast.ImportDeclaration;
import de.uka.iti.ivil.jml.parser.ast.LineComment;
import de.uka.iti.ivil.jml.parser.ast.PackageDeclaration;
import de.uka.iti.ivil.jml.parser.ast.TypeParameter;
import de.uka.iti.ivil.jml.parser.ast.body.AnnotationDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.AnnotationMemberDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.ClassOrInterfaceDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.ConstructorDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.EmptyMemberDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.EmptyTypeDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.EnumConstantDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.EnumDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.FieldDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.InitializerDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.InvariantDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.JavadocComment;
import de.uka.iti.ivil.jml.parser.ast.body.MethodDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.Parameter;
import de.uka.iti.ivil.jml.parser.ast.body.VariableDeclarator;
import de.uka.iti.ivil.jml.parser.ast.body.VariableDeclaratorId;
import de.uka.iti.ivil.jml.parser.ast.expr.ArrayAccessExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ArrayCreationExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ArrayInitializerExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.AssignExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.BinaryExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.BooleanLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.CastExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.CharLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ClassExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ConditionalExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.DoubleLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.EnclosedExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.FieldAccessExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.InstanceOfExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.IntegerLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.IntegerLiteralMinValueExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.LongLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.LongLiteralMinValueExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.MarkerAnnotationExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.MemberValuePair;
import de.uka.iti.ivil.jml.parser.ast.expr.MethodCallExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NormalAnnotationExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NullLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ObjectCreationExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.QualifiedNameExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.QuantificationExpression;
import de.uka.iti.ivil.jml.parser.ast.expr.SingleMemberAnnotationExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.StringLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.SuperExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ThisExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.TypeExpression;
import de.uka.iti.ivil.jml.parser.ast.expr.TypeRelationExpression;
import de.uka.iti.ivil.jml.parser.ast.expr.UnaryExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.VariableDeclarationExpr;
import de.uka.iti.ivil.jml.parser.ast.spec.AnyFieldExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.ArrayRangeExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.JMLStatement;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodSpecification;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreEverythingExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreRefExpression;
import de.uka.iti.ivil.jml.parser.ast.stmt.AssertStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.BlockStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.BreakStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.CatchClause;
import de.uka.iti.ivil.jml.parser.ast.stmt.ContinueStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.DoStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.EmptyStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.ExpressionStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.ForStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.ForeachStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.IfStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.LabeledStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.ReturnStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.SwitchEntryStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.SwitchStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.SynchronizedStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.ThrowStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.TryStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.TypeDeclarationStmt;
import de.uka.iti.ivil.jml.parser.ast.stmt.WhileStmt;
import de.uka.iti.ivil.jml.parser.ast.type.ClassOrInterfaceType;
import de.uka.iti.ivil.jml.parser.ast.type.PrimitiveType;
import de.uka.iti.ivil.jml.parser.ast.type.ReferenceType;
import de.uka.iti.ivil.jml.parser.ast.type.VoidType;
import de.uka.iti.ivil.jml.parser.ast.type.WildcardType;

/**
 * @author Julio Vilmar Gesser
 */
public interface GenericVisitor<R, A> {

    //- Compilation Unit ----------------------------------

    public R visit(CompilationUnit compilationUnit, A arg);

    public R visit(PackageDeclaration n, A arg);

    public R visit(ImportDeclaration n, A arg);

    public R visit(TypeParameter n, A arg);

    public R visit(LineComment n, A arg);

    public R visit(BlockComment n, A arg);

    //- Body ----------------------------------------------

    public R visit(ClassOrInterfaceDeclaration n, A arg);

    public R visit(EnumDeclaration n, A arg);

    public R visit(EmptyTypeDeclaration n, A arg);

    public R visit(EnumConstantDeclaration n, A arg);

    public R visit(AnnotationDeclaration n, A arg);

    public R visit(AnnotationMemberDeclaration n, A arg);

    public R visit(FieldDeclaration n, A arg);

    public R visit(VariableDeclarator n, A arg);

    public R visit(VariableDeclaratorId n, A arg);

    public R visit(ConstructorDeclaration n, A arg);

    public R visit(MethodDeclaration n, A arg);

    public R visit(Parameter n, A arg);

    public R visit(EmptyMemberDeclaration n, A arg);

    public R visit(InitializerDeclaration n, A arg);

    public R visit(JavadocComment n, A arg);

    //- Type ----------------------------------------------

    public R visit(ClassOrInterfaceType n, A arg);

    public R visit(PrimitiveType n, A arg);

    public R visit(ReferenceType n, A arg);

    public R visit(VoidType n, A arg);

    public R visit(WildcardType n, A arg);

    //- Expression ----------------------------------------

    public R visit(ArrayAccessExpr n, A arg);

    public R visit(ArrayCreationExpr n, A arg);

    public R visit(ArrayInitializerExpr n, A arg);

    public R visit(AssignExpr n, A arg);

    public R visit(BinaryExpr n, A arg);

    public R visit(CastExpr n, A arg);

    public R visit(ClassExpr n, A arg);

    public R visit(ConditionalExpr n, A arg);

    public R visit(EnclosedExpr n, A arg);

    public R visit(FieldAccessExpr n, A arg);

    public R visit(InstanceOfExpr n, A arg);

    public R visit(StringLiteralExpr n, A arg);

    public R visit(IntegerLiteralExpr n, A arg);

    public R visit(LongLiteralExpr n, A arg);

    public R visit(IntegerLiteralMinValueExpr n, A arg);

    public R visit(LongLiteralMinValueExpr n, A arg);

    public R visit(CharLiteralExpr n, A arg);

    public R visit(DoubleLiteralExpr n, A arg);

    public R visit(BooleanLiteralExpr n, A arg);

    public R visit(NullLiteralExpr n, A arg);

    public R visit(MethodCallExpr n, A arg);

    public R visit(NameExpr n, A arg);

    public R visit(ObjectCreationExpr n, A arg);

    public R visit(QualifiedNameExpr n, A arg);

    public R visit(ThisExpr n, A arg);

    public R visit(SuperExpr n, A arg);

    public R visit(StoreEverythingExpression n, A arg);

    public R visit(ArrayRangeExpression n, A arg);

    public R visit(AnyFieldExpression n, A arg);

    public R visit(StoreRefExpression n, A arg);

    public R visit(UnaryExpr n, A arg);

    public R visit(VariableDeclarationExpr n, A arg);

    public R visit(MarkerAnnotationExpr n, A arg);

    public R visit(SingleMemberAnnotationExpr n, A arg);

    public R visit(NormalAnnotationExpr n, A arg);

    public R visit(MemberValuePair n, A arg);

    public R visit(QuantificationExpression n, A arg);

    public R visit(TypeExpression n, A arg);

    public R visit(TypeRelationExpression n, A arg);

    //- Statements ----------------------------------------

    public R visit(ExplicitConstructorInvocationStmt n, A arg);

    public R visit(TypeDeclarationStmt n, A arg);

    public R visit(AssertStmt n, A arg);

    public R visit(BlockStmt n, A arg);

    public R visit(LabeledStmt n, A arg);

    public R visit(EmptyStmt n, A arg);

    public R visit(ExpressionStmt n, A arg);

    public R visit(SwitchStmt n, A arg);

    public R visit(SwitchEntryStmt n, A arg);

    public R visit(BreakStmt n, A arg);

    public R visit(ReturnStmt n, A arg);

    public R visit(IfStmt n, A arg);

    public R visit(WhileStmt n, A arg);

    public R visit(ContinueStmt n, A arg);

    public R visit(DoStmt n, A arg);

    public R visit(ForeachStmt n, A arg);

    public R visit(ForStmt n, A arg);

    public R visit(ThrowStmt n, A arg);

    public R visit(SynchronizedStmt n, A arg);

    public R visit(TryStmt n, A arg);

    public R visit(CatchClause n, A arg);

    public R visit(JMLStatement n, A arg);

    // - Specifications ------------------------------------

    public R visit(MethodSpecification n, A arg);

    public R visit(MethodContract n, A arg);

    public R visit(InvariantDeclaration n, A arg);
}
