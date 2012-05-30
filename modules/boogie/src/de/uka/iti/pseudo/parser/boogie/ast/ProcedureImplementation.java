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
import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeParameter;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class ProcedureImplementation extends DeclarationBlock implements NamedASTElement {

    private final String name;
    private final List<ASTTypeParameter> typeParameters;
    private final List<VariableDeclaration> inParam, outParam;
    private final ProcedureBody body;

    public ProcedureImplementation(Token first, List<Attribute> attr, Token name, List<Token> typeParameters,
            List<VariableDeclaration> inParam, List<VariableDeclaration> outParam, ProcedureBody body) {
        super(first, attr);

        this.name = ASTConversions.getEscapedName(name);
        this.inParam = inParam;
        this.outParam = outParam;
        this.body = body;

        this.typeParameters = new ArrayList<ASTTypeParameter>(typeParameters.size());
        for (Token t : typeParameters)
            this.typeParameters.add(new ASTTypeParameter(t));

        addChildren(this.typeParameters);
        addChildren(inParam);
        addChildren(outParam);
        addChild(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<ASTTypeParameter> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    public List<VariableDeclaration> getOutParameters() {
        return Collections.unmodifiableList(outParam);
    }

    public List<VariableDeclaration> getInParameters() {
        return Collections.unmodifiableList(inParam);
    }

    public ProcedureBody getBody() {
        return body;
    }
}
