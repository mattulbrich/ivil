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
package de.uka.iti.ivil.jbc.util.parsers.ast.type;

/**
 * @author Julio Vilmar Gesser
 */
public final class ReferenceType extends Type {

    private Type type;

    private int arrayCount;

    public ReferenceType() {
    }

    public ReferenceType(Type type) {
        this.type = type;
    }

    public ReferenceType(Type type, int arrayCount) {
        this.type = type;
        this.arrayCount = arrayCount;
    }

    public ReferenceType(int beginLine, int beginColumn, int endLine, int endColumn, Type type, int arrayCount) {
        super(beginLine, beginColumn, endLine, endColumn);
        this.type = type;
        this.arrayCount = arrayCount;
    }

    public int getArrayCount() {
        return arrayCount;
    }

    public Type getType() {
        return type;
    }

    public void setArrayCount(int arrayCount) {
        this.arrayCount = arrayCount;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public <A> void visit(TypeVisitor<A> visitor, A arg) {
        visitor.visit(this, arg);
    }

}
