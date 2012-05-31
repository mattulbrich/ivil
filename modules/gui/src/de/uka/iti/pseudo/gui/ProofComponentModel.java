/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import nonnull.Nullable;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.util.IntList;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.TextInstantiator;
import de.uka.iti.pseudo.util.Util;

/**
 * A proof component model is a specialised tree model for the ProofComponent
 *
 * @see ProofComponent
 */
class ProofComponentModel extends DefaultTreeModel {

    private static final long serialVersionUID = -6525872309302128760L;

    /**
     * Use this label to not branch this branch but continue it on the main
     * branch.
     */
    private static final String CONTINUATION_LABEL = "...";

    /**
     * The verbosity of this node in the tree
     */
    private int verbosity;

    /**
     * flag which indicates whether the numbers of nodes are to be printed in
     * front of the label.
     */
    private boolean showNumbers;

    /**
     * The pretty printer used to instantiate schema vars in labels.
     */
    private final PrettyPrint prettyPrint;

    /**
     * The proof center to which the ProofComponent belongs.
     */
    private final ProofCenter proofCenter;

    /**
     * A TreeNode corresponding to one proof node of the proof.
     */
    public class ProofTreeNode implements TreeNode {

        /**
         * the proofnode associated with this tree node. There may be 2 tree
         * nodes per ProofNode. (one leaf, one inner node)
         */
        private final ProofNode proofNode;

        /**
         * my children in the tree. May be null if the tree node has not yet
         * been extended.
         */
        private @Nullable
        List<ProofTreeNode> children;

        /**
         * The "uplink" in the tree. <code>null</code> for the root.
         */
        private @Nullable
        final
        ProofTreeNode parent;

        /**
         * The cached label of the node. <code>null</code> if the label has not
         * yet been constructed, or if it has been reset.
         */
        private @Nullable
        String label;

        public ProofTreeNode(ProofTreeNode parent, ProofNode proofNode,
                boolean leaf) {
            this.parent = parent;
            this.proofNode = proofNode;
            if (leaf) {
                children = Collections.emptyList();
            }
        }

        //
        // -- implementation of the interface
        //

        @Override
        public Enumeration<?> children() {
            expand();
            return Collections.enumeration(children);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            expand();
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            expand();
            return children.size();
        }

        @Override
        public int getIndex(TreeNode node) {
            expand();
            return children.indexOf(node);
        }

        // NB: narrowing of return type!
        @Override
        public ProofTreeNode getParent() {
            return parent;
        }

        @Override
        public boolean isLeaf() {
            expand();
            return children.isEmpty();
        }

        //
        // --- now my own methods
        //

        /**
         * Expand the tree on demand.
         *
         * Do nothing, if the children have already been calculated. Otherwise,
         * calculate all children.
         *
         * @see #calculateChildren()
         */
        private void expand() {
            if (children != null) {
                return;
            }

            children = calculateChildren();
        }

        /**
         * Calculate children for an inner node.
         *
         * Must not be called on a leaf.
         *
         * @return a fresh list of proof tree nodes.
         */
        private List<ProofTreeNode> calculateChildren() {
            List<ProofTreeNode> result = new ArrayList<ProofTreeNode>();
            ProofNode node = proofNode;

            while (node != null) {

                if (shouldShow(node)) {
                    result.add(new ProofTreeNode(this, node, true));
                }

                List<ProofNode> nodeChildren = node.getChildren();

                if (nodeChildren == null) {
                    // closed goal
                    node = null;
                } else if (nodeChildren.size() == 1) {
                    // exactly one successor ... serial next
                    node = nodeChildren.get(0);
                } else {
                    node = null;
                    for (ProofNode pn : nodeChildren) {
                        if (node == null
                                && CONTINUATION_LABEL
                                        .equals(getBranchLabel(pn))) {
                            node = pn;
                        } else {
                            result.add(new ProofTreeNode(this, pn, false));
                        }
                    }
                }
            }

            return result;
        }

        /**
         * Recalculate the tree structure.
         *
         * The differences to the existing children list in analysed and
         * according events are fired. Revalidate also all children.
         */
        public void revalidate() {
            // TODO this is O(n^2). We could do better, probably.

            // invalidate label
            label = null;
            nodeChanged(this);

            // do nothing if not expanded
            if (children == null) {
                return;
            }

            // do nothing in case of a leaf
            if (children.isEmpty()) {
                return;
            }

            List<ProofTreeNode> newChildren = calculateChildren();

            // iterate the old children to find the removals.
            IntList removedIndices = new IntList();
            List<ProofTreeNode> removedNodes = new ArrayList<ProofTreeNode>();
            for (int i = 0; i < children.size();) {
                ProofTreeNode node = children.get(i);
                if (findInList(node, newChildren)) {
                    i++;
                } else {
                    children.remove(i);
                    removedIndices.add(i);
                    removedNodes.add(node);
                }
            }

            if (!removedIndices.isEmpty()) {
                nodesWereRemoved(this, removedIndices.toIntArray(),
                        removedNodes.toArray());
            }

            // iterate the new children to find the insertions.
            IntList addedIndices = new IntList();
            for (int i = 0; i < newChildren.size(); i++) {
                ProofTreeNode node = newChildren.get(i);
                if (!findInList(node, children)) {
                    children.add(i, node);
                    addedIndices.add(i);
                }
            }

            if (!addedIndices.isEmpty()) {
                nodesWereInserted(this, addedIndices.toIntArray());
            }

            // recursion into all children.
            for (ProofTreeNode child : children) {
                child.revalidate();
            }

        }

        /*
         * check whether verbosity makes us show this node: - verbosity of node
         * <= set verbosity
         */
        private boolean shouldShow(ProofNode node) {
            RuleApplication ruleApp = node.getAppliedRuleApp();
            if (ruleApp == null) {
                return true;
            }

            if (node.getChildren() == null) {
                return true;
            }

            // Always show the root formula
            if (proofCenter.getProof().getRoot() == node) {
                return true;
            }

            Rule rule = ruleApp.getRule();
            String string = rule.getProperty(RuleTagConstants.KEY_VERBOSITY);
            int value;
            try {
                value = Util.parseUnsignedInt(string);
            } catch (NumberFormatException e) {
                return true;
            }

            return value <= verbosity;
        }

        public ProofNode getProofNode() {
            return proofNode;
        }

        /**
         * Gets a {@link TreePath} which leads from the root to this.
         *
         * @return a freshly created {@link TreePath} object.
         */
        public TreePath getPath() {
            if (parent == null) {
                return new TreePath(this);
            } else {
                return parent.getPath().pathByAddingChild(this);
            }
        }

        /**
         * recursively clear the cached labels.
         */
        public void invalidateLabels() {
            label = null;
            if (children != null) {
                for (ProofTreeNode child : children) {
                    child.invalidateLabels();
                }
            }
        }

        /**
         * Find a proof node carrying a certain proof node.
         *
         * @param pn
         *            proof node to find a tree node for.
         * @param expandOnDemand
         *            expand the tree during the search iff <code>true</code>.
         *
         * @return <code>null</code> if no node found, otherwise a treenode for
         *         <code>pn</code>.
         */
        public ProofTreeNode findProofNode(ProofNode pn, boolean expandOnDemand) {
            if (pn == getProofNode()
                    && (children == null || children.isEmpty())) {
                return this;
            }

            if (children == null) {
                if (expandOnDemand) {
                    expand();
                } else {
                    return null;
                }
            }

            for (ProofTreeNode child : children) {
                ProofTreeNode res = child.findProofNode(pn, expandOnDemand);
                if (res != null) {
                    return res;
                }
            }

            return null;
        }

        /*
         * get the label to this node: IF it is an inner node with children of
         * its own THEN return #getBranchName() IF it is a leaf THEN IF it has
         * no applied rule yet THEN return OPEN ELSE return the name of the
         * applied rule.
         */
        public String getLabel() {
            if (label == null) {
                if (!isLeaf()) {
                    label = getBranchLabel(proofNode);
                } else {
                    RuleApplication appliedRuleApp = proofNode
                            .getAppliedRuleApp();
                    if (appliedRuleApp != null) {
                        label = getApplicationLabel();
                        // label = appliedRuleApp.getRule().getName();
                    } else {
                        label = "OPEN";
                    }
                }

                // empty vector indicates a leaf.
                if (showNumbers && children != null && children.isEmpty()
                        && parent != null) {
                    label = proofNode.getNumber() + ": " + label;
                }
            }

            return label;
        }

        /*
         * Application label is either the name of the applied rule if no
         * "display" property is set or the instantiated display property of the
         * applied rule.
         */
        private String getApplicationLabel() {

            RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();
            Rule rule = appliedRuleApp.getRule();
            String displayString = rule
                    .getProperty(RuleTagConstants.KEY_DISPLAY);
            if (displayString != null) {
                return instantiateString(appliedRuleApp, displayString);
            } else {
                return rule.getName();
            }

        }

        /*
         * Branch names give a name to inner nodes: Extract it from the the rule
         * and the index of the branch. If no name is set on the cases, use
         * "branch" + index
         */
        private String getBranchLabel(ProofNode node) {
            ProofNode parent = node.getParent();

            if (parent == null) {
                return "";
            }

            int index = parent.getChildren().indexOf(node);
            assert index != -1;

            RuleApplication appliedRuleApp = parent.getAppliedRuleApp();

            // just in case ...
            if (appliedRuleApp == null) {
                return "branch " + (index + 1);
            }

            Rule rule = appliedRuleApp.getRule();
            GoalAction ga = rule.getGoalActions().get(index);
            String actionName = ga.getName();

            if (actionName == null) {
                return "branch" + (index + 1);
            } else {
                return instantiateString(appliedRuleApp, actionName);
            }
        }

        @Override
        public String toString() {
            return label + "/"
                    + (proofNode == null ? -1 : proofNode.getNumber());
        }

    }

    public ProofComponentModel(ProofNode root, ProofCenter proofCenter) {
        // we cannot do this in one call, since this would give a comp. error
        super(null);
        setRoot(new ProofTreeNode(null, root, false));

        this.proofCenter = proofCenter;
        this.prettyPrint = proofCenter.getPrettyPrinter();
    }

    /**
     * Retrieve the proof node from a tree path object.
     *
     * @param path
     *            a path into this model
     * @return the proof stored at this path
     */
    public ProofNode getProofNode(TreePath path) {
        ProofTreeNode treeNode = (ProofTreeNode) path.getLastPathComponent();
        return treeNode.getProofNode();
    }

    /**
     * Publish changes that have been made to the model.
     *
     * This merely triggers a revalidation of the root node.
     *
     * Alternatively, the set of nodes to be reexamined could be calculated from
     * the argument. (If performance is poor).
     *
     * @param changesProofNodes
     *            the changes proof nodes
     */
    public void publishChanges(List<ProofNode> changesProofNodes) {
        assert SwingUtilities.isEventDispatchThread();
        ((ProofTreeNode) root).revalidate();
    }

    /**
     * Retrieve a tree node for a proof node
     *
     * @param node
     *            a proof node to find
     * @param allowExpansion
     *            if <code>true</code>, expand tree nodes on demand.
     * @return the proof stored at this path
     */
    public ProofTreeNode getProofTreeNode(ProofNode node, boolean allowExpansion) {
        ProofTreeNode ptroot = (ProofTreeNode) root;
        ProofTreeNode found = ptroot.findProofNode(node, allowExpansion);
        return found;
    }

    /**
     * Gets a tree path for a proof node.
     *
     * @param node
     *            a node in the proof.
     * @param allowExpansion
     *            the allow expansion
     *
     * @return the freshly created path, <code>null</code> if none found.
     */
    public TreePath getPath(ProofNode node, boolean allowExpansion) {
        ProofTreeNode found = getProofTreeNode(node, allowExpansion);

        if (found == null) {
            return null;
        } else {
            return found.getPath();
        }
    }

    /**
     * Sets the verbosity of the display.
     *
     * The tree is revalidated afterwards.
     *
     * @param verbosity
     *            the new verbosity value.
     */
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
        ((ProofTreeNode) root).revalidate();
    }

    /**
     * Sets whether node numbers are to be indicated in labels or not.
     *
     * It merely sets the flag. Triggering a repaint event has to be done
     * outside.
     *
     * @param b
     *            the new value.
     */
    public void setShowNumbers(boolean b) {
        Log.enter(b);
        showNumbers = b;
        ((ProofTreeNode) root).invalidateLabels();
    }

    /*
     * instantiate the schema variables in a string.
     */
    private String instantiateString(RuleApplication ruleApp, String string) {
        TextInstantiator textInst = new TextInstantiator(ruleApp);
        return textInst.replaceInString(string, prettyPrint);
    }

    /*
     * find in a list of ProofTreeNodes a tree node which belongs to the same proof node.
     */
    private static boolean findInList(ProofTreeNode node,
            List<ProofTreeNode> list) {
        ProofNode proofNode = node.getProofNode();
        for (ProofTreeNode ptn : list) {
            if (ptn.getProofNode() == proofNode) {
                return true;
            }
        }
        return false;
    }

}
