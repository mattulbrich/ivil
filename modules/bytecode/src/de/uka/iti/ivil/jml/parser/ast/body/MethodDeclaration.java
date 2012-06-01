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
package de.uka.iti.ivil.jml.parser.ast.body;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.ivil.jml.parser.ast.TypeParameter;
import de.uka.iti.ivil.jml.parser.ast.expr.AnnotationExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.BinaryExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.BinaryExpr.Operator;
import de.uka.iti.ivil.jml.parser.ast.expr.BooleanLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NullLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreRefExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.Line;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.LineType;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodSpecification;
import de.uka.iti.ivil.jml.parser.ast.stmt.BlockStmt;
import de.uka.iti.ivil.jml.parser.ast.type.PrimitiveType;
import de.uka.iti.ivil.jml.parser.ast.type.Type;
import de.uka.iti.ivil.jml.parser.ast.type.VoidType;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Julio Vilmar Gesser
 */
public final class MethodDeclaration extends BodyDeclaration {

    private int modifiers;

    private List<TypeParameter> typeParameters;

    private Type type;

    private String name;

    private final List<Parameter> parameters;

    private int arrayCount;

    private List<NameExpr> throws_;

    private BlockStmt body;

    private final MethodSpecification methodSpec;

    public MethodDeclaration(int modifiers, Type type, String name) {
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.methodSpec = null;
        this.parameters = new ArrayList<Parameter>(0);
    }

    public MethodDeclaration(int modifiers, Type type, String name, List<Parameter> parameters) {
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.parameters = (parameters != null ? parameters : new ArrayList<Parameter>(0));
        this.methodSpec = null;
    }

    public MethodDeclaration(JavadocComment javaDoc, int modifiers, List<AnnotationExpr> annotations,
            List<TypeParameter> typeParameters, Type type, String name, List<Parameter> parameters, int arrayCount,
            List<NameExpr> throws_, BlockStmt block) {
        super(annotations, javaDoc);
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.type = type;
        this.name = name;
        this.parameters = (parameters != null ? parameters : new ArrayList<Parameter>(0));
        this.arrayCount = arrayCount;
        this.throws_ = throws_;
        this.body = block;
        this.methodSpec = null;
    }

    public MethodDeclaration(int beginLine, int beginColumn, int endLine, int endColumn, JavadocComment javaDoc,
            MethodSpecification methodSpec, int modifiers, List<AnnotationExpr> annotations,
            List<TypeParameter> typeParameters, Type type, String name, List<Parameter> parameters, int arrayCount,
            List<NameExpr> throws_, BlockStmt block) {
        super(beginLine, beginColumn, endLine, endColumn, annotations, javaDoc);
        this.methodSpec = methodSpec;
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.type = type;
        this.name = name;
        this.parameters = (parameters != null ? parameters : new ArrayList<Parameter>(0));
        this.arrayCount = arrayCount;
        this.throws_ = throws_;
        this.body = block;

        // ensure presence of a method specification
        if (null == methodSpec)
            methodSpec = new MethodSpecification();

        // process pure, if present
        if (ModifierSet.isPure(modifiers)) {

            // add assignable and diverges to EACH contract
            Line assignable = new Line(LineType.assignable, new ArrayList<StoreRefExpression>(0));
            Line diverges = new Line(LineType.diverges, new BooleanLiteralExpr(false));
            for (MethodContract m : methodSpec.getContracts()) {
                m.get(LineType.assignable).add(assignable);
                m.get(LineType.diverges).add(diverges);
            }
        }

        // process non_null(nullable) result
        if (!ModifierSet.isNullable(modifiers) && !(type instanceof VoidType || type instanceof PrimitiveType)) {

            // add assignable and diverges to EACH contract
            Line ensures = new Line(LineType.ensures, new BinaryExpr(new NameExpr("\\result"), new NullLiteralExpr(),
                    Operator.notEquals));
            for (MethodContract m : methodSpec.getContracts()) {
                m.get(LineType.ensures).add(ensures);
            }

        }

        // process nullable in formal paramters
        if (null != parameters)
            for (Parameter p : parameters) {
                if (!ModifierSet.isNullable(modifiers)
                        && !(p.getType() instanceof VoidType || p.getType() instanceof PrimitiveType)) {

                    Line requires = new Line(LineType.ensures, new BinaryExpr(new NameExpr(p.getId().getName()),
                            new NullLiteralExpr(), Operator.notEquals));
                    for (MethodContract m : methodSpec.getContracts()) {
                        m.get(LineType.requires).add(requires);
                    }
                }
            }
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public int getArrayCount() {
        return arrayCount;
    }

    public BlockStmt getBody() {
        return body;
    }

    /**
     * Return the modifiers of this member declaration.
     * 
     * @see ModifierSet
     * @return modifiers
     */
    public int getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<NameExpr> getThrows() {
        return throws_;
    }

    public Type getType() {
        return type;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public void setArrayCount(int arrayCount) {
        this.arrayCount = arrayCount;
    }

    public void setBody(BlockStmt body) {
        this.body = body;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters.clear();
        this.parameters.addAll(parameters);
    }

    public void setThrows(List<NameExpr> throws_) {
        this.throws_ = throws_;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTypeParameters(List<TypeParameter> typeParameters) {
        this.typeParameters = typeParameters;
    }

    public boolean hasMethodSpec() {
        return null != methodSpec;
    }

    public MethodSpecification getMethodSpec() {
        assert null != methodSpec : "this method has no specification";
        return methodSpec;
    }
}
