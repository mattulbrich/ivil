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
package de.uka.iti.pseudo.proof;

import java.util.ArrayList;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Sequent;

/**
 * The Class SequentHistory provides a mean to gather information on how a
 * particular formula in a proof node has evolved.
 * 
 * It is a tag which is kept for every formula in the sequent. For changed
 * formulas, the tag is renewed by one referring to the old one, but with
 * additional information (text and link to node).
 * 
 * Objects of this class are stored in {@link ProofNode}s, furnished with data
 * during the construction of a node and are then fixed using {@link #fix()} to
 * make the structure immutable afterwards.
 */
public class SequentHistory {

    /**
     * A {@link SequentHistory} annotation keeps history information about one
     * step in a proof.
     */
    public static class Annotation {

        /**
         * Either the name of a rule or an instantiation of the
         * {@value RuleTagConstants#KEY_DISPLAY} property of a rule.
         */
        private String text;

        /**
         * The proof node due to which the change has been made.
         */
        private ProofNode creatingProofNode;

        /**
         * The parent annotation. The one with which the formula was previously
         * tagged.
         */
        private Annotation parentAnnotation;

        /**
         * Instantiates a new annotation.
         * 
         * @param text
         *            the text
         * @param creatingProofNode
         *            the creating proof node
         * @param parentAnnotation
         *            the parent annotation
         */
        public Annotation(String text, ProofNode creatingProofNode,
                Annotation parentAnnotation) {
            this.text = text;
            this.creatingProofNode = creatingProofNode;
            this.parentAnnotation = parentAnnotation;
        }

        /**
         * Instantiates a new annotation without parent and w/o proof node
         * 
         * @param text
         *            the text
         */
        public Annotation(String text) {
            this.text = text;
        }

        /**
         * get the text that comes along with this annotation
         * 
         * @return the description, not null
         */
        public @NonNull
        String getText() {
            return text;
        }

        /**
         * get the proof node which gave raise to this annotation
         * 
         * @return a proof node or null if there is no such node
         */
        public @Nullable
        ProofNode getCreatingProofNode() {
            return creatingProofNode;
        }

        /**
         * get the parent annotation for this annotation. This may be null if
         * the annotation is toplevel
         * 
         * @return the parent annotation or null
         */
        public @Nullable
        Annotation getParentAnnotation() {
            return parentAnnotation;
        }

        public String toString() {
            return "Annotation[text=" + text + "; proofNode = "
                    + creatingProofNode + "]";
        }
    }

    // TODO: Auto-generated Javadoc
    // XXX: HOW THE HECK DOES THIS WORK?!?!?

    /**
     * The text of the rule application which has been made
     */
    private String ruleAppText;

    /**
     * The annotation which is reason for the last change.
     */
    private Annotation reasonAnnotation;

    /**
     * The creating proof node.
     */
    private ProofNode creatingProofNode;

    /**
     * The fixed.
     */
    private boolean fixed;

    /**
     * The antecedent.
     */
    private ArrayList<Annotation> antecedent;

    /**
     * The succedent.
     */
    private ArrayList<Annotation> succedent;

    /**
     * Instantiates a new sequent history.
     * 
     * @param ruleAppText
     *            the rule app text
     * @param reasonAnnotation
     *            the reason annotation
     * @param creatingProofNode
     *            the creating proof node
     */
    public SequentHistory(String ruleAppText, Annotation reasonAnnotation,
            ProofNode creatingProofNode) {
        this.ruleAppText = ruleAppText;
        this.reasonAnnotation = reasonAnnotation;
        this.creatingProofNode = creatingProofNode;
        this.antecedent = new ArrayList<Annotation>();
        this.succedent = new ArrayList<Annotation>();
    }

    /**
     * Instantiates a new sequent history.
     * 
     * @param sequentHistory
     *            the sequent history
     * @param ruleAppText
     *            the rule app text
     * @param reasonAnnotation
     *            the reason annotation
     * @param proofNode
     *            the proof node
     */
    public SequentHistory(SequentHistory sequentHistory, String ruleAppText,
            Annotation reasonAnnotation, ProofNode proofNode) {
        this(ruleAppText, reasonAnnotation, proofNode);
        antecedent.addAll(sequentHistory.antecedent);
        succedent.addAll(sequentHistory.succedent);
    }

    /**
     * Instantiates a new sequent history.
     * 
     * @param sequent
     *            the sequent
     * @param initialAnnotation
     *            the initial annotation
     */
    public SequentHistory(Sequent sequent, Annotation initialAnnotation) {
        this.antecedent = new ArrayList<Annotation>();
        int len = sequent.getAntecedent().size();
        for (int i = 0; i < len; i++) {
            antecedent.add(initialAnnotation);
        }

        this.succedent = new ArrayList<Annotation>();
        len = sequent.getSuccedent().size();
        for (int i = 0; i < len; i++) {
            succedent.add(initialAnnotation);
        }

        fix();
    }

    /**
     * Fix.
     */
    public void fix() {
        fixed = true;
        ruleAppText = null;
        creatingProofNode = null;
        reasonAnnotation = null;
        antecedent.trimToSize();
        succedent.trimToSize();
    }

    /**
     * Check not fixed.
     */
    private void checkNotFixed() {
        if (fixed)
            throw new IllegalStateException(
                    "must not change a fixed SequentHistory");
    }

    /**
     * Sizes agree with.
     * 
     * @param sequent
     *            the sequent
     * 
     * @return true, if successful
     */
    public boolean sizesAgreeWith(Sequent sequent) {
        return antecedent.size() == sequent.getAntecedent().size()
                && succedent.size() == sequent.getSuccedent().size();
    }

    /**
     * Removed.
     * 
     * @param selector
     *            the selector
     */
    public void removed(TermSelector selector) {
        checkNotFixed();

        if (selector.isAntecedent()) {
            antecedent.remove(selector.getTermNo());
        } else {
            succedent.remove(selector.getTermNo());
        }
    }

    /**
     * Added.
     * 
     * @param side
     *            the side
     */
    public void added(boolean side) {
        checkNotFixed();

        Annotation ann = new Annotation(ruleAppText, creatingProofNode,
                reasonAnnotation);

        if (side == TermSelector.ANTECEDENT) {
            antecedent.add(ann);
        } else {
            succedent.add(ann);
        }

    }

    /**
     * Select.
     * 
     * @param selector
     *            the selector
     * 
     * @return the annotation
     */
    public Annotation select(TermSelector selector) {
        if (selector == null)
            return null;
        else if (selector.isAntecedent())
            return antecedent.get(selector.getTermNo());
        else
            return succedent.get(selector.getTermNo());
    }

    /**
     * Replaced.
     * 
     * @param selector
     *            the selector
     */
    public void replaced(TermSelector selector) {
        checkNotFixed();

        Annotation ann = new Annotation(ruleAppText, creatingProofNode,
                reasonAnnotation);

        if (selector.isAntecedent())
            antecedent.set(selector.getTermNo(), ann);
        else
            succedent.set(selector.getTermNo(), ann);
    }

}
