/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast.type;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.NamedASTElement;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

final public class UserTypeDefinition extends ASTElement implements NamedASTElement {
    
    private final Token location;
    private final String name;
    // this is only needed to process type table construction, as the argnames
    // can specify
    private final List<ASTTypeParameter> argnames;
    private final ASTType definition;

    public UserTypeDefinition(Token name, List<Token> argnames, ASTType parent) {
        this.name = ASTConversions.getEscapedName(name);
        this.location = name;
        this.definition = parent;

        this.argnames = new ArrayList<ASTTypeParameter>(argnames.size());
        for (Token t : argnames)
            this.argnames.add(new ASTTypeParameter(t));
        
        addChildren(this.argnames);

        if (null != parent)
            addChild(parent);
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<ASTTypeParameter> getTypeParameters() {
        return argnames;
    }

    public ASTType getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "UserTypeDefinition [" + name + "] @" + location.beginColumn;
    }
}
