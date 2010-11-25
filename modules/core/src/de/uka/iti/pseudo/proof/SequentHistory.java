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
 * 
 * This class keeps two lists {@link #antecedent} and {@link #succedent} holding
 * references to annotations. The lists have the same lengths as the according
 * lists of the associated sequent.
 */
public class SequentHistory {

    /**
     * A {@link SequentHistory} annotation keeps history information about one
     * step in a proof. This class is immutable.
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

    /**
     * This annotation is generated during the construction and will be used to
     * modify the history information. It is set to null when the object is
     * fixed.
     */
    private Annotation protoType;
    //private String ruleAppText;

    /**
     * Indicates that the sequent history has been marked fixed, i.e., made
     * immutable.
     */
    private boolean fixed;

    /**
     * The list of annotations for the antecedent.
     */
    private ArrayList<Annotation> antecedent;

    /**
     * The list of annotations for the succedent.
     */
    private ArrayList<Annotation> succedent;

    /**
     * Instantiates a new sequent history using information for the creation of
     * annotations.
     * 
     * {@link #antecedent} and {@link #succedent} are set to empty lists.
     * 
     * @param ruleAppText
     *            the text used for new annotations
     * @param reasonAnnotation
     *            the parent annotation used for new annotations
     * @param creatingProofNode
     *            the creating proof node used for new annotations
     */
    public SequentHistory(String ruleAppText, Annotation reasonAnnotation,
            ProofNode creatingProofNode) {
        this.protoType = new Annotation(ruleAppText, creatingProofNode, reasonAnnotation);
        this.antecedent = new ArrayList<Annotation>();
        this.succedent = new ArrayList<Annotation>();
    }

    /**
     * Instantiates a new sequent history using information for the creation of
     * annotations and a parent sequent history. {@link #antecedent} and
     * {@link #succedent} are copies of the lists in <code>sequentHistory</code>.
     * 
     * @param sequentHistory
     *            the sequent history used for initialisation
     * @param ruleAppText
     *            the text used for new annotations
     * @param reasonAnnotation
     *            the parent annotation used for new annotations
     * @param creatingProofNode
     *            the creating proof node used for new annotations
     */
    public SequentHistory(SequentHistory sequentHistory, String ruleAppText,
            Annotation reasonAnnotation, ProofNode proofNode) {
        this(ruleAppText, reasonAnnotation, proofNode);
        antecedent.addAll(sequentHistory.antecedent);
        succedent.addAll(sequentHistory.succedent);
    }

    /**
     * Instantiates a new sequent history using an initial annotation.
     * 
     * The lists of annotations contains the initial annotation. Their lengths
     * correspond to the lengths of the sequent. The history is fixed
     * automatically.
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
     * Make this sequent history immutable.
     * 
     * The structure cannot be changed after a call to this method.
     * 
     * Construction references are set to null.
     */
    public void fix() {
        fixed = true;
        protoType = null;
        antecedent.trimToSize();
        succedent.trimToSize();
    }

    /**
     * Throws an exception if the history has been fixed already.
     */
    private void checkNotFixed() {
        if (fixed)
            throw new IllegalStateException(
                    "must not change a fixed SequentHistory");
    }

    /**
     * Checks whether the sizes of the sized of the antecedent and succedent
     * list coincide with the corresponding lists of the given sequent.
     * 
     * @param sequent
     *            the sequent to check against
     * 
     * @return true, if successful
     */
    public boolean sizesAgreeWith(@NonNull Sequent sequent) {
        return antecedent.size() == sequent.getAntecedent().size()
                && succedent.size() == sequent.getSuccedent().size();
    }

    /**
     * Modify the lists of annotations by removing an annotation. The position
     * of the annotation is given by a term selector.
     * 
     * @param selector
     *            a term selector indicating the index of the annotation to
     *            remove.
     */
    public void removed(@NonNull TermSelector selector) {
        checkNotFixed();

        if (selector.isAntecedent()) {
            antecedent.remove(selector.getTermNo());
        } else {
            succedent.remove(selector.getTermNo());
        }
    }

    /**
     * Modify the lists of annotations by adding a new annotation. The data to
     * use in the annotation is taken from the prototype. 
     * 
     * @param side
     *            the side to add to. {@link TermSelector#ANTECEDENT} or
     *            {@link TermSelector#SUCCEDENT}.
     */
    public void added(boolean side) {
        checkNotFixed();

        if (side == TermSelector.ANTECEDENT) {
            antecedent.add(protoType);
        } else {
            succedent.add(protoType);
        }
    }

    /**
     * Modify the lists of annotations by replacing an annotation. The data to
     * use in the annotation is taken from the prototype.
     * 
     * @param selector
     *            a term selector indicating the index of the annotation to
     *            replace.
     */
    public void replaced(@NonNull TermSelector selector) {
        checkNotFixed();

        if (selector.isAntecedent())
            antecedent.set(selector.getTermNo(), protoType);
        else
            succedent.set(selector.getTermNo(), protoType);
    }
    
    /**
     * Retrieve an annotation for certain position.
     * 
     * Returns <code>null</code> only if the argument is <code>null</code>.
     * 
     * @param selector
     *            a term selector indicating the index of the annotation to
     *            retrieve.
     * 
     * @return the annotation at the given position or null
     */
    public Annotation select(TermSelector selector) {
        if (selector == null)
            return null;
        else if (selector.isAntecedent())
            return antecedent.get(selector.getTermNo());
        else
            return succedent.get(selector.getTermNo());
    }

}
