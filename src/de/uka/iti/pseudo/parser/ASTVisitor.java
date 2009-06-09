/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser;

import de.uka.iti.pseudo.parser.file.ASTFileVisitor;
import de.uka.iti.pseudo.parser.term.ASTTermVisitor;

/**
 * The Interface ASTVisitor is part of the visitor pattern for Term ASTs.
 * 
 * The implementing visit methods may throw ASTVisitExceptions if their
 * visitation fails.
 */
public interface ASTVisitor extends ASTTermVisitor, ASTFileVisitor {
}
