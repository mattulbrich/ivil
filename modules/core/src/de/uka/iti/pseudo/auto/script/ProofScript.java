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
import de.uka.iti.pseudo.proof.ProofIdentifier;

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

    /**
     * The proof obligation to which this scripts belongs.
     */
    private final @NonNull ProofIdentifier obligation;

    /**
     * The root of the proof-script tree.
     */
    private final @NonNull ProofScriptNode root;


    /**
     * Instantiates a new proof script.
     *
     * @param obligation
     *            the obligation to proof
     * @param node
     *            the root of the proof script tree
     */
    public ProofScript(@NonNull ProofIdentifier obligation, @NonNull ProofScriptNode node) {
        this.obligation = obligation;
        this.root = node;
    }

    /**
     * Gets the proof obligation to which this scripts belongs.
     *
     * @return the obligation
     */
    public @NonNull ProofIdentifier getObligation() {
        return obligation;
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
