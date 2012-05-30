/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
//package de.uka.iti.pseudo.environment.creation;
//
//import de.uka.iti.pseudo.term.Term;
//import de.uka.iti.pseudo.term.TermException;
//
//public interface ReplacingCloneableTermVisitor extends Cloneable {
//
//    /**
//     * @return this, if the replacer is state less, a clone otherwise
//     */
//    public ReplacingCloneableTermVisitor copy();
//
//    /**
//     * Can be called after a call to copy to replace all instances of toReplace
//     * in replaceIn by replaceWith.
//     *
//     * The visitor <b>must be reusable</b> after replace terminated.
//     *
//     * @param toReplace
//     * @param replaceWith
//     * @param replaceIn
//     *
//     * @return a copy of replaceIn where toReplace was substituted by
//     *         replaceWith or replaceIn iff toReplace does not occur in
//     *         replaceIn
//     *
//     * @throws TermException
//     *             might be thrown if the desired replacement would be illegal
//     */
//    public Term replace(Term toReplace, Term replaceWith, Term replaceIn)
//            throws TermException;
//}
