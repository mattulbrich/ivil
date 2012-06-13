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

/**
 * Modality type as attributed to a {@link ProgramTerm}.
 *
 * Each constants carries with it the delimiter character sequences to open and
 * close the modality.
 *
 * There are three modalities: BOX, BOX_TERMINATION and DIAMOND. Additionally
 * there is the ANY modality which matches against any of the other three and
 * can only be used in {@link SchemaProgramTerm} and not in
 * {@link LiteralProgramTerm}.
 *
 * @see ProgramTerm#getModality()
 */
public enum Modality {

    /**
     * The box modality <tt>[&middot;]</tt>. Means: Every finite run is
     * successful.
     */
    BOX("[", "]"),

    /**
     * The terminating box modality <tt>[[&middot;]]</tt>. Means: Every run is
     * finite and succesful.
     */
    BOX_TERMINATION("[[", "]]"),

    /**
     * The diamond modality <tt>[&lt;&middot;&gt;]</tt>. The dual to
     * {@link #BOX}.
     */
    DIAMOND("[<", ">]"),

    /**
     * The matching modality <tt>[?&middot;?]</tt>. It matches against any other
     * modality and must only be used in schematic terms.
     */
    ANY("[?", "?]");

    /**
     * The opening delimiter (.
     */
    private final String openingDelim;

    /**
     * The closing delimiter ).
     */
    private final String closingDelim;

    /**
     * Instantiates a new modality.
     *
     * @param opening the opening delimiter
     * @param closing the closing delimiter
     */
    private Modality(@NonNull String opening, @NonNull String closing) {
        this.openingDelim = opening;
        this.closingDelim = closing;
    }

    /**
     * Gets the opening character sequence for this modality.
     *
     * @return the opening delimiter
     */
    public @NonNull String getOpeningDelimiter() {
        return openingDelim;
    }

    /**
     * Gets the closing character sequence for this modality..
     *
     * @return the closing delimiter
     */
    public @NonNull String getClosingDelimiter() {
        return closingDelim;
    }

}
