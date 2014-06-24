/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.script;

import nonnull.NonNull;

/**
 * A Proof script is a tree-structure containing individual proof steps. This
 * class captures the script as a whole and keeps the proof obligation to which
 * it belongs.
 *
 * @see ProofScriptNode
 * @see ProofScriptCommand
 * @see ProofIdentifier
 */

public class ProofScript {

    // will move to ProofObligation eventually
    public static final String EMPTY_IDENTIFIER = "none";

    /**
     * The proof obligation to which this scripts belongs.
     */
    private final @NonNull String obligationIdentifier;

    /**
     * The root of the proof-script tree.
     */
    private final @NonNull ProofScriptNode root;


    /**
     * Instantiates a new proof script.
     *
     * @param obligation
     *            the key (qualified name) of the obligation to which this proof
     *            belongs
     * @param node
     *            the root of the proof script tree
     */
    public ProofScript(@NonNull String key, @NonNull ProofScriptNode node) {
        this.obligationIdentifier = key;
        this.root = node;
    }

    /**
     * Gets the key of the proof obligation to which this scripts belongs.
     *
     * @return the obligation
     */
    public @NonNull String getObligationIdentifier() {
        return obligationIdentifier;
    }

    /**
     * Gets the root of the proof-script tree.
     *
     * @return the root
     */
    public @NonNull ProofScriptNode getRoot() {
        return root;
    }

}
