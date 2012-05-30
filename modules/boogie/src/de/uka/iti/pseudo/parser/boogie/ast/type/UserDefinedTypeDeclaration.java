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

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.DeclarationBlock;

public class UserDefinedTypeDeclaration extends DeclarationBlock {

    public UserDefinedTypeDeclaration(Token firstToken, List<Attribute> attributes, List<UserTypeDefinition> definitions) {
        super(firstToken, attributes);

        addChildren(attributes);
        addChildren(definitions);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
