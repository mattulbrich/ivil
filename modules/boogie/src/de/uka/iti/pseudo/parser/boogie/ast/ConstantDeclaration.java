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

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class ConstantDeclaration extends DeclarationBlock {

    private final boolean unique;
    private final List<VariableDeclaration> names;
    private final List<ExtendsParent> parents;
    private final boolean complete;

    public ConstantDeclaration(Token firstToken, List<Attribute> attributes, boolean unique,
            List<VariableDeclaration> names,
            List<ExtendsParent> parents, boolean complete) {
        super(firstToken, attributes);
        this.unique = unique;
        this.names = names;
        this.parents = parents;
        this.complete = complete;

        addChildren(names);
        if (null != parents)
            addChildren(parents);
    }

    public boolean isUnique() {
        return unique;
    }

    public List<VariableDeclaration> getNames() {
        return Collections.unmodifiableList(names);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean hasExtends() {
        return null != parents;
    }

    public List<ExtendsParent> getParents() {
        assert null != parents : "you missed a check";
        return parents;
    }

    public boolean isComplete() {
        return complete;
    }

}
