/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTPluginDeclaration extends ASTElement {

    private Token serviceName;
    private Token implementationClass;
    
    public ASTPluginDeclaration(Token serviceName, Token implementationClass) {
        super();
        this.serviceName = serviceName;
        this.implementationClass = implementationClass;
    }

    @Override public Token getLocationToken() {
        return serviceName;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getServiceName() {
        return serviceName;
    }

    public Token getImplementationClass() {
        return implementationClass;
    }

}
