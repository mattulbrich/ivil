/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;

public enum Modality {

    BOX("[", "]"),
    BOX_TERMINATION("[[", "]]"),
    DIAMOND("[<", ">]"),
    ANY("[?", "?]")
    ;
    
    private final String openingDelim;
    private final String closingDelim;

    private Modality(@NonNull String opening, @NonNull String closing) {
        this.openingDelim = opening;
        this.closingDelim = closing;
    }

    /**
     * @return the opening
     */
    public @NonNull String getOpeningDelimiter() {
        return openingDelim;
    }

    /**
     * @return the closing
     */
    public @NonNull String getClosingDelimiter() {
        return closingDelim;
    }
    
}
