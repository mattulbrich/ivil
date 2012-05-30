/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeParameter;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public class FunctionDeclaration extends DeclarationBlock implements NamedASTElement {

    private final String name;

    private final List<ASTTypeParameter> typeParameters;
    private final List<VariableDeclaration> inParameters;
    private final VariableDeclaration outParemeter;

    // ! @note: can be null if a function has no specification
    private final Expression expression;

    public FunctionDeclaration(Token firstToken, List<Attribute> attributes, Token name, List<Token> typeParameters,
            List<VariableDeclaration> inParam, VariableDeclaration outParam, Expression expression) {
        super(firstToken, attributes);
        this.name = ASTConversions.getEscapedName(name);

        this.inParameters = inParam;
        this.outParemeter = outParam;

        this.expression = expression;

        this.typeParameters = new ArrayList<ASTTypeParameter>(typeParameters.size());
        for (Token n : typeParameters)
            this.typeParameters.add(new ASTTypeParameter(n));

        addChildren(this.typeParameters);
        addChildren(inParam);
        addChild(outParam);

        if (null != expression)
            addChild(expression);
    }


    public String getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public String toString(){
        return "FunctionDeclaration [" + name + ", " + getLocation() + "]";
    }


    public VariableDeclaration getOutParemeter() {
        return outParemeter;
    }


    public List<VariableDeclaration> getInParameters() {
        return inParameters;
    }


    public List<ASTTypeParameter> getTypeParameters() {
        return typeParameters;
    }
}
