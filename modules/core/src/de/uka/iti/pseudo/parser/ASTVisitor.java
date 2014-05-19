/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser;

import de.uka.iti.pseudo.parser.file.ASTFileVisitor;
import de.uka.iti.pseudo.parser.program.ASTProgramVisitor;
import de.uka.iti.pseudo.parser.term.ASTTermVisitor;

/**
 * The Interface ASTVisitor is part of the visitor pattern for Term ASTs.
 *
 * The implementing visit methods may throw ASTVisitExceptions if their
 * visitation fails.
 */
// TODO consider making ASTVisitor generic
// public interface ASTVisitor<R> or ASTVisitor<R,P>
public interface ASTVisitor extends ASTTermVisitor, ASTFileVisitor, ASTProgramVisitor {

}
