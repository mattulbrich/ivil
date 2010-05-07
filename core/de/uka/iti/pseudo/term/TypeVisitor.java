/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

/**
 * Allow visiting also for type structures. Even though this is a rather small
 * distinction it can make since in combination with for instance TermVisitor.
 */
public interface TypeVisitor {

    Type visit(TypeApplication typeApplication) throws TermException;
    
    Type visit(TypeVariable typeVariable) throws TermException;

}
