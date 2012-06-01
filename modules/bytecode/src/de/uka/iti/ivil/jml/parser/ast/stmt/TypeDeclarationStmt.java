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
 * Created on 04/11/2006
 */
package de.uka.iti.ivil.jml.parser.ast.stmt;

import de.uka.iti.ivil.jml.parser.ast.body.TypeDeclaration;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * @author Julio Vilmar Gesser
 */
public final class TypeDeclarationStmt extends Statement {

    private TypeDeclaration typeDecl;

    public TypeDeclarationStmt() {
    }

    public TypeDeclarationStmt(TypeDeclaration typeDecl) {
        this.typeDecl = typeDecl;
    }

    public TypeDeclarationStmt(int beginLine, int beginColumn, int endLine, int endColumn, TypeDeclaration typeDecl) {
        super(beginLine, beginColumn, endLine, endColumn);
        this.typeDecl = typeDecl;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public TypeDeclaration getTypeDeclaration() {
        return typeDecl;
    }

    public void setTypeDeclaration(TypeDeclaration typeDecl) {
        this.typeDecl = typeDecl;
    }
}
