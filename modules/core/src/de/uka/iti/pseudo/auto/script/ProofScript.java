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
import de.uka.iti.pseudo.util.Pair;

/**
 * A Proof script is a tree-structure containing individual proof steps. This
 * class captures the script as a whole and keeps the proof obligation to which
 * it belongs.
 *
 * @see ProofScriptNode
 * @see ProofScriptCommand
 */

public class ProofScript {

    /**
     * The proof obligation to which this scripts belongs.
     */
    private final @NonNull Obligation obligation;

    /**
     * The root of the proof-script tree.
     */
    private final @NonNull ProofScriptNode root;

    /**
     * The kind of proof obligations that are considered. Together with a name
     * they uniquely identify the object which is subject to a proof script.
     */
    public enum Kind {
        /**
         * The proof obligation is for the justification of a rule.
         */
        RULE, /**
         * The proof obligation is for the justification of a lemma/sequent/problem.
         */
        PROBLEM, /**
         * The proof obligation is for the justification of a program
         */
        PROGRAM
    };

    /**
     * Obligations are immutable pairs of a {@link Kind} and a name.
     */
    public final static class Obligation extends Pair<Kind, String>{

        /**
         * Instantiates a new obligation.
         *
         * @param kind
         *            the kind of th obligation
         * @param name
         *            the name of the obligation
         */
        public Obligation(@NonNull Kind kind, @NonNull String name) {
            super(kind, name);
        }

        /**
         * Gets the kind.
         *
         * @return the kind
         */
        public @NonNull Kind getKind() {
            return fst();
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public @NonNull String getName() {
            return snd();
        }

    }

    /**
     * Instantiates a new proof script.
     *
     * @param obligation
     *            the obligation to proof
     * @param node
     *            the root of the proof scriüt tree
     */
    public ProofScript(@NonNull Obligation obligation, @NonNull ProofScriptNode node) {
        this.obligation = obligation;
        this.root = node;
    }

    /**
     * Gets the proof obligation to which this scripts belons.
     *
     * @return the obligation
     */
    public @NonNull Obligation getObligation() {
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
