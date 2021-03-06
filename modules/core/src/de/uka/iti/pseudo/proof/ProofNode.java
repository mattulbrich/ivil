/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.proof.SequentHistory.Annotation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.rule.meta.MetaEvaluator;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.ProgramComparingTermInstantiator;
import de.uka.iti.pseudo.term.creation.SubtermReplacer;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;


/**
 * A ProofNode describes a single node in the sequent calculus proof tree.
 *
 * <p>
 * It has an immutable sequent attached to it and stores references to the
 * parent node in the tree and to all children nodes it possesses. This links
 * (the children in particular) may change throughout the life time of a proof
 * node.
 *
 * <p>
 * A ProofNode is an open node if it has an open leaf node as a successor. A
 * leaf is open, if it has set the children field to "null". Only the root node
 * has parent reference which is null.
 *
 * <p>
 * The apply method allows the associated proof to apply a rule on a leaf of the
 * tree. It is checked whether all clauses fit the current circumstances. If
 * they do the tree is expanded and new nodes are added as children.
 *
 * <p>
 * A proof node carries information about the history of the terms in its
 * sequent. This information is updated and the information is kept track of.
 *
 * @see Proof
 */
public class ProofNode implements Comparable<ProofNode> {

    /**
     * The sequent stored in this node. This does not change.
     */
    private final Sequent sequent;

    /**
     * The associated proof.
     */
    private final Proof proof;

    /**
     * The children nodes. This is null if this does not have any child nodes yet
     */
    private ProofNode /*@Nullable*/ [] children = null;

    /**
     * The parent of this node in the proof tree
     */
    private @Nullable ProofNode parent;

    /*@ invariant (\forall ProofNode n; n != null &&
     *@  (\exists int i; i >= 0 & i < children.length; children[i] == n);
     *@  n.parent == this); @*/

    /**
     * The applied rule.
     * This is set iff children have been calculated
     */
    private @Nullable ImmutableRuleApplication appliedRuleApp = null;

    /**
     * The locally added symbols which may appear in this proof node.
     * It is a fixed table.
     */
    private final @NonNull SymbolTable symbolTable;

    /*@ invariant appliedRule == null <==> children == null; @*/

    /**
     * This node's number which is unique throughout the proof.
     */
    private final int number;

    /*@ invariant (\forall ProofNode n; n != null && n.proof == proof;
     *@     n.number == number ==> this == n); */

    /**
     * The sequent history.
     * This object is fixed in the constructor and cannot
     * be changed afterwards.
     */
    private final SequentHistory sequentHistory;

    /**
     * This constructor is called when starting a new proof with an initial
     * sequent. It is only package visible since this is to be called only from
     * {@link Proof}.
     *
     * @param proof
     *            the proof to which this node belongs
     * @param parent
     *            the parent node in the proof tree
     * @param sequent
     *            the sequent of this node
     * @param initialAnnotation
     *            the annotation which is used for all terms in the sequent to
     *            initially tag the sequent.
     * @param env
     */
    ProofNode(@NonNull Proof proof, @NonNull Sequent sequent,
            @NonNull SequentHistory.Annotation initialAnnotation, Environment env) {
        this(proof, null, sequent,
                new SequentHistory(sequent, initialAnnotation),
                new SymbolTable(env));
    }

    /*
     * This constructor is called during rule application.
     *
     * It sets the attributes and asserts certain properties.
     */
    private ProofNode(@NonNull Proof proof, @Nullable ProofNode parent,
            @NonNull Sequent sequent,
            @NonNull SequentHistory history,
            @NonNull SymbolTable symbolTable) {
        this.proof = proof;
        this.parent = parent;
        this.sequent = sequent;
        this.symbolTable = symbolTable;
        this.number = proof.makeFreshNumber();
        this.sequentHistory = history;

        sequentHistory.fix();
        symbolTable.setFixed();

        assert sequentHistory.sizesAgreeWith(sequent);
        assert parent == null || parent.proof == proof;
    }

    //
    // various getter methods
    //

    /**
     * Gets the children nodes of this node.
     *
     * <p>Returns null iff this node is an open leaf.
     *
     * @return an unmodifiable list of proof nodes or null
     */
    public @Nullable List<ProofNode> getChildren() {
        if(children != null) {
            return Util.readOnlyArrayList(children);
        } else {
            return null;
        }
    }

    /**
     * Gets the rule application that has been applied to this
     * node.
     *
     * <p>Returns null iff this node is an open leaf.
     *
     * @return the applied rule or null
     */
    public @Nullable RuleApplication getAppliedRuleApp() {
        return appliedRuleApp;
    }

    /**
     * Gets the parent node of the proof tree
     *
     * <p>Returns null iff this node is the root of the proof tree.
     *
     * @return the parent node or null
     */
    public @Nullable ProofNode getParent() {
        return parent;
    }

    /**
     * Gets the sequent in this proof node.
     *
     * @return the sequent
     */
    public @NonNull Sequent getSequent() {
        return sequent;
    }

    /**
     * get the number of this proof node in the proof.
     *
     * It is unique within the proof and used as index in RuleApplications. It
     * does not change troughout the lifetime of this bject.
     *
     * @return the non-negative unique index of this proof node
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets a string summarizing this node.
     *
     * <p>
     * It tells whether this node is open or closed and about its size. The
     * string is used as label in {@link GoalList} objects.
     *
     * @return the summary string
     */
    public @NonNull String getSummaryString() {
        StringBuilder sb = new StringBuilder();

        sb.append(number).append(": ");
        if(appliedRuleApp != null) {
            sb.append("closed ");
        } else {
            sb.append("open ");
        }

        sb.append("node, ")
        .append(sequent.getAntecedent().size())
        .append(" |- ").append(sequent.getSuccedent().size());

        return sb.toString();
    }



    /**
     * Checks if this node is closed.
     *
     * <p>
     * A node is closed if there is no open leaf under it. A leaf is open iff
     * its children field is set to null
     *
     * @return true iff is closed
     */
    public boolean isClosed() {
        if (children == null) {
            return false;
        }

        for (ProofNode child : children) {
            if (!child.isClosed()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the associated proof.
     *
     * @return the proof
     */
    public @NonNull Proof getProof() {
        return proof;
    }

    /**
     * Collect all open goals under this node into a collection.
     *
     * @param openGoals
     *            a list to add open goals to.
     */
    void collectOpenGoals(@NonNull List<? super ProofNode> openGoals) {
        if(children == null) {
            openGoals.add(this);
        } else {
            for (ProofNode child : children) {
                child.collectOpenGoals(openGoals);
            }
        }
    }

    //
    // methods to modify the tree.
    // They are protected under the synchronisation of their proof object.
    //

    /**
     * Remove any child from this node and set the applied rule to null.
     * Additionally, set the parent of all children to null.
     *
     * <p>This method is only package visible and should only be called from within
     * {@link Proof#prune(ProofNode)} which is a synchronised method.
     */
    void prune() {
        if(children != null) {
            for (ProofNode node : children) {
                node.parent = null;
            }
        }

        children = null;
        appliedRuleApp = null;
    }

    /**
     * Checks whether ruleApp can be applied or not.
     */
    public boolean applicable(RuleApplicationCertificate ruleApp) {
        try{
            if (appliedRuleApp != null) {
                throw new ProofException("Trying to apply proof to a non-leaf proof node");
            }

            ruleApp.ensureVerified();

            doActions(ruleApp, ruleApp.getEnvironment());
        } catch (ProofException e) {
            return false;
        }
        return true;
    }

    /**
     * Apply a {@link RuleApplication} to this node. The find, where and assume
     * clauses are checked the action clauses executed.
     *
     * <p>An immutable instance of the rule application is stored in this proof
     * node.
     *
     * <p>This method is only package visible and should only be called from within
     * {@link Proof#apply(RuleApplication, Environment)} which is a
     * synchronised method.
     *
     * @param ruleApp
     * @param inst
     * @param env
     * @param whereClauseProperties
     * @throws ProofException
     */
    void apply(RuleApplicationCertificate rac) throws ProofException {
        if(appliedRuleApp != null) {
            throw new ProofException("Trying to apply proof to a non-leaf proof node");
        }

        rac.ensureVerified();

        Environment env = rac.getEnvironment();
        children = doActions(rac, env);

        if(!rac.hasMonotoneProperties()) {
            throw new ProofException("The application has modified existing properties");
        }

        ImmutableRuleApplication immRuleApp;
        immRuleApp = new ImmutableRuleApplication(rac);
        this.appliedRuleApp = immRuleApp;
    }

    /*
     * Execute the action parts of a rule.
     *
     * A list of new proof nodes is returned. Each corresponds to one goal
     * description in the action part of a rule and its sequent contains the
     * adequate modification of the original sequent. Each contains an updated
     * history track.
     */
    private ProofNode[] doActions(RuleApplication ruleApp, Environment env)
            throws ProofException {

        Rule rule = ruleApp.getRule();
        TermInstantiator inst = new ProgramComparingTermInstantiator(
                ruleApp.getSchemaVariableMapping(),
                ruleApp.getTypeVariableMapping(),
                ruleApp.getSchemaUpdateMapping(),
                env);

        List<ProofNode> newNodes = new LinkedList<ProofNode>();
        List<Term> antecedent = new ArrayList<Term>();
        List<Term> succedent = new ArrayList<Term>();
        MetaEvaluator metaEval = new MetaEvaluator(ruleApp, env);
        String ruleAppText = makeRuleAppAnnotation(rule, inst);
        TermSelector findSelector = ruleApp.getFindSelector();
        Annotation reasonAnnotation = null;
        if(findSelector != null) {
            reasonAnnotation = sequentHistory.select(findSelector);
        }

        try {

            for (GoalAction action : rule.getGoalActions()) {
                antecedent.clear();
                succedent.clear();
                metaEval.resetLocalSymbols();
                SequentHistory history;
                switch (action.getKind()) {
                case CLOSE:
                    return new ProofNode[0];
                case COPY:
                    antecedent.addAll(sequent.getAntecedent());
                    succedent.addAll(sequent.getSuccedent());
                    history = new SequentHistory(sequentHistory, ruleAppText,
                            reasonAnnotation, this);
                    break;
                default:
                    history = new SequentHistory(ruleAppText, reasonAnnotation, this);
                    break;
                }

                Term replaceWith = action.getReplaceWith();

                if(replaceWith != null) {
                    Term instantiated = inst.instantiate(replaceWith);
                    instantiated = metaEval.evalutate(instantiated);
                    replaceTerm(findSelector, instantiated, antecedent, succedent);
                    history.replaced(findSelector);
                } else if(action.isRemoveOriginalTerm()) {
                    assert findSelector.isToplevel();
                    if(findSelector.isAntecedent()) {
                        antecedent.remove(findSelector.getTermNo());
                    } else {
                        succedent.remove(findSelector.getTermNo());
                    }
                    history.removed(findSelector);
                }

                for (Term add : action.getAddAntecedent()) {
                    Term toAdd = inst.instantiate(add);
                    toAdd = metaEval.evalutate(toAdd);
                    antecedent.add(toAdd);
                    history.added(TermSelector.ANTECEDENT);
                }

                for (Term add : action.getAddSuccedent()) {
                    Term toAdd = inst.instantiate(add);
                    toAdd = metaEval.evalutate(toAdd);
                    succedent.add(toAdd);
                    history.added(TermSelector.SUCCEDENT);
                }

                SymbolTable lst = metaEval.getLocalSymbolTable();
                if(lst.equals(this.symbolTable)) {
                    // save memory by reusing the same object!
                    lst = symbolTable;
                }
                Sequent seq = new Sequent(antecedent, succedent);
                newNodes.add(new ProofNode(proof, this, seq, history, lst));
            }

            return Util.listToArray(newNodes, ProofNode.class);

        } catch (TermException e) {
            Log.log(Log.WARNING, "Failed rule application:");
            if(Log.isLogging(Log.DEBUG)) {
                Dump.dumpRuleApplication(ruleApp);
            }
            throw new ProofException("Exception during application of rule " + rule.getName(), e);
        }
    }

    /*
     * Extract an textual annotation from a rule description.
     *
     * If the rule has got a tag "display" this value has its {%c} schema
     * references instantiated and is returned.
     *
     * The name of the rule is returned if there is no such tag.
     */
    private String makeRuleAppAnnotation(Rule rule, TermInstantiator inst) {
        String annotation = rule.getProperty(RuleTagConstants.KEY_DISPLAY);
        if(annotation == null) {
            return rule.getName();
        } else {
            return annotation;
        }
    }

    /*
     * Replace a term in a sequent.
     *
     * The term is given by a term selector and the sequent as two lists.
     */
    private void replaceTerm(TermSelector sel, Term replaceWith,
            List<Term> antecedent, List<Term> succedent) throws ProofException,
            TermException {
        Term oldTerm = sel.selectTopterm(sequent);
        Term newTerm;

        if (!sel.isToplevel()) {
            newTerm = SubtermReplacer.replace(oldTerm, sel.getSubtermSelector(), replaceWith);
        } else {
            newTerm = replaceWith;
        }

        int index = sel.getTermNo();
        if (sel.isAntecedent()) {
            antecedent.set(index, newTerm);
        } else {
            succedent.set(index, newTerm);
        }
    }

    /**
     * Gets the history for all term in the sequent.
     *
     * @return the sequent history
     */
    public @NonNull SequentHistory getSequentHistory() {
        return sequentHistory;
    }

    @Override
    public String toString() {
        return "ProofNode #" + getSummaryString();
    }

    @Override
    public int compareTo(ProofNode o) {
        return number - o.number;
    }

    //    /**
    //     * Get the list of program code locations which occur in the sequent. The
    //     * result of the search is cached in this object to make this operation less
    //     * expensive.
    //     *
    //     * @return an immutable view to the list of code locations.
    //     */
    //    public Set<CodeLocation<Program>> getCodeLocations() {
    //        if(codeLocations == null) {
    //            codeLocations = CodeLocation.findCodeLocations(sequent);
    //        }
    //        return Collections.unmodifiableSet(codeLocations);
    //    }

    public SymbolTable getLocalSymbolTable() {
        return symbolTable;
    }
}