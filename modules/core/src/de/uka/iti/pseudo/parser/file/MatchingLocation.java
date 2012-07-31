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

/**
 * The Enum MatchingLocation allows to specify the location of a term with
 * respect to the "infers" sign "|-".
 *
 * TODO Move this to package rule.
 */
public enum MatchingLocation {

    /**
     * <b>LEFT</b> of the <code>|-</code>
     */
    ANTECEDENT,

    /**
     * <b>RIGHT</b> of the <code>|-</code>
     */
    SUCCEDENT,

    /**
     * Either left or right. Often this implies that the term may also be
     * subterm of another term.
     */
    BOTH

}
