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
import de.uka.iti.ivil.jml.parser.ast.expr.BooleanLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.Line;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.LineType;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodSpecification;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreRefExpression;
import de.uka.iti.ivil.jml.parser.ast.stmt.BlockStmt;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Julio Vilmar Gesser
 */
public final class ConstructorDeclaration extends BodyDeclaration {

    private int modifiers;

    private List<TypeParameter> typeParameters;

    private String name;

    private final List<Parameter> parameters;

    private List<NameExpr> throws_;

    private BlockStmt block;

    private final MethodSpecification methodSpec;

    public ConstructorDeclaration() {
        this.methodSpec = null;
        this.parameters = new ArrayList<Parameter>();
    }

    public ConstructorDeclaration(int modifiers, String name) {
        this.modifiers = modifiers;
        this.name = name;
        this.methodSpec = null;
        this.parameters = new ArrayList<Parameter>();
    }

    public ConstructorDeclaration(JavadocComment javaDoc, int modifiers, List<AnnotationExpr> annotations, List<TypeParameter> typeParameters, String name, List<Parameter> parameters, List<NameExpr> throws_, BlockStmt block) {
        super(annotations, javaDoc);
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.name = name;
        this.parameters = (parameters == null ? new ArrayList<Parameter>() : parameters);
        this.throws_ = throws_;
        this.block = block;
        this.methodSpec = null;
    }

    public ConstructorDeclaration(int beginLine, int beginColumn, int endLine, int endColumn, JavadocComment javaDoc,
            MethodSpecification methodSpec, int modifiers, List<AnnotationExpr> annotations,
            List<TypeParameter> typeParameters, String name, List<Parameter> parameters, List<NameExpr> throws_,
            BlockStmt block) {
        super(beginLine, beginColumn, endLine, endColumn, annotations, javaDoc);
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.name = name;
        this.parameters = (parameters == null ? new ArrayList<Parameter>() : parameters);
        this.throws_ = throws_;
        this.block = block;
        this.methodSpec = methodSpec;

        if (null != methodSpec) {
            // process pure, if present
            if (ModifierSet.isPure(modifiers)) {
                if (0 == methodSpec.getContracts().size()) {
                    // create a contract, as no other contract has been
                    // specified

                    List<Line> lines = new ArrayList<Line>(2);
                    // FIXME this is not correct in case of constructors!
                    lines.add(new Line(LineType.assignable, new ArrayList<StoreRefExpression>(0)));
                    lines.add(new Line(LineType.diverges, new BooleanLiteralExpr(false)));
                    methodSpec.getContracts().add(new MethodContract(lines));
                } else {
                    // add assignable and diverges to EACH contract
                    Line assignable = new Line(LineType.assignable, new ArrayList<StoreRefExpression>(0));
                    Line diverges = new Line(LineType.diverges, new BooleanLiteralExpr(false));
                    for (MethodContract m : methodSpec.getContracts()) {
                        m.get(LineType.assignable).add(assignable);
                        m.get(LineType.diverges).add(diverges);
                    }
                }
            }

            // nullable is not processed, because it has no meaning on
            // constructors
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

    public BlockStmt getBlock() {
        return block;
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

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public void setBlock(BlockStmt block) {
        this.block = block;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setThrows(List<NameExpr> throws_) {
        this.throws_ = throws_;
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
