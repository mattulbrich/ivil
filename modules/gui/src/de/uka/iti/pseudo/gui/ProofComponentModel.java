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
package de.uka.iti.pseudo.gui;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.TextInstantiator;

/**
 * A proof component model is a specialised tree model for the ProofComponent
 *
 * @see ProofComponent
 */
class ProofComponentModel extends DefaultTreeModel {
    
    private static final long serialVersionUID = -6525872309302128760L;
    
    private static final String CONTINUATION_LABEL = "...";
    
    private final static Vector<ProofTreeNode> EMPTY_VECTOR = new Vector<ProofTreeNode>();
    
    private int verbosity;
    
    private boolean showNumbers;

    private PrettyPrint prettyPrint;

    private ProofCenter proofCenter;

    // we assume that only the AWT thread operates on this data structure, hence,
    // no synchronisation is needed.
    private Queue<ProofNode> updateQueue = new LinkedList<ProofNode>();
    
    /**
     * A TreeNode corresponding to one proof node of the proof. 
     */
    public class ProofTreeNode implements TreeNode {

    	private ProofNode proofNode;
        private Vector<ProofTreeNode> children;
        private ProofTreeNode parent;
        private String label;
        
        public ProofTreeNode(ProofTreeNode parent, ProofNode proofNode, boolean leaf) {
            this.parent = parent;
            this.proofNode = proofNode;
            if(leaf)
                children = EMPTY_VECTOR;
        }

        //
        // -- implementation of the interface
        //
        
        @Override
        public Enumeration<?> children() {
            expand();
            return children.elements();
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            expand();
            return children.elementAt(childIndex);
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

        private void expand() {
            if(children != null)
                return;
            
            children = new Vector<ProofTreeNode>();
            ProofNode node = proofNode;
            
            while(node != null) {
                
                if(shouldShow(node))
                	children.add(new ProofTreeNode(this, node, true));
                
                List<ProofNode> nodeChildren = node.getChildren();
            
                if(nodeChildren == null) {
                	// closed goal
                	node = null;
                } else if(nodeChildren.size() == 1) {
                	// exactly one successor ... serial next
                	node = nodeChildren.get(0);
                } else {
                	node = null;
                	for (ProofNode pn : nodeChildren) {
                		if(node == null && CONTINUATION_LABEL.equals(getBranchLabel(pn))) {
                			node = pn;
                		} else {
                			children.add(new ProofTreeNode(this, pn, false));
                		}
                	}
                }
            }
        }

        /*
         * check whether verbosity makes us show this node:
         * - verbosity of node <= set verbosity
         */
        
        private boolean shouldShow(ProofNode node) {
            RuleApplication ruleApp = node.getAppliedRuleApp();
            if(ruleApp == null)
                return true;
            
            if(node.getChildren() == null)
                return true;
            
            // Always show the root formula
            if(proofCenter.getProof().getRoot() == node)
                return true;
            
            Rule rule = ruleApp.getRule();
            String string = rule.getProperty(RuleTagConstants.KEY_VERBOSITY);
            int value;
            try {
                value = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return true;
            }
            
            return value <= verbosity;
        }

        public ProofNode getProofNode() {
            return proofNode;
        }
        
        public TreePath getPath() {
            if(parent == null) {
                return new TreePath(this);
            } else {
                return parent.getPath().pathByAddingChild(this);
            }
        }

        public void invalidate() {
            children = null;
            label = null;
        }
        
        public ProofTreeNode findProofNode(ProofNode pn, boolean expandOnDemand) {
            if(pn == getProofNode() && (children == null || children.isEmpty()))
                return this;
            
            if(children == null) {
                if(expandOnDemand) {
                    expand();
                } else {
                    return null;
                }
            }
            
            for (ProofTreeNode child : children) {
                ProofTreeNode res = child.findProofNode(pn, expandOnDemand);
                if(res != null)
                    return res;
            }
            
            return null;
        }

        /*
         * get the label to this node:
         * IF it is an inner node with children of its own THEN return #getBranchName()
         * IF it is a leaf THEN
         *   IF it has no applied rule yet 
         *   THEN return OPEN 
         *   ELSE return the name of the applied rule.
         */
        public String getLabel() {
            if(label == null) {
                if(!isLeaf()) {
                    label = getBranchLabel(proofNode);
                } else {
                    RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();
                    if(appliedRuleApp != null) {
                        label = getApplicationLabel();
                        // label = appliedRuleApp.getRule().getName();
                    } else {
                        label = "OPEN";
                    }
                }
                
                // empty vector indicates a leaf.
                if(showNumbers && children != null && children.isEmpty() && parent != null) {
                    label = proofNode.getNumber() + ": " + label;
                }
            }
            
            return label;
        }

        /*
         * Application label is either the name of the applied rule if no
         * "display" property is set or the instantiated display property
         * of the applied rule.
         */
        private String getApplicationLabel() {
            
            RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();
            Rule rule = appliedRuleApp.getRule();
            String displayString = rule.getProperty(RuleTagConstants.KEY_DISPLAY);
            if(displayString != null)
                return instantiateString(appliedRuleApp, displayString);
            else
                return rule.getName();
            
        }

        /*
         * Branch names give a name to inner nodes: Extract it from the the rule
         * and the index of the branch. If no name is set on the cases, use
         * "branch" + index 
         */
		private String getBranchLabel(ProofNode node) {
			ProofNode parent = node.getParent();
            
            if(parent == null)
                return "";
            
            int index = parent.getChildren().indexOf(node);
            assert index != -1;
            
            RuleApplication appliedRuleApp = parent.getAppliedRuleApp();
            
            // just in case ...
            if(appliedRuleApp == null)
                return "branch " + (index+1);
            
            Rule rule = appliedRuleApp.getRule();
            GoalAction ga = rule.getGoalActions().get(index);
            String actionName = ga.getName();
            
            if(actionName == null)
                return "branch" + (index + 1);
            else
                return instantiateString(appliedRuleApp, actionName);
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
     * retrieve the proof node from a tree path object.
     * 
     * @param path a path into this model
     * @return the proof stored at this path
     */
    public ProofNode getProofNode(TreePath path) {
        ProofTreeNode treeNode = (ProofTreeNode) path.getLastPathComponent();
        return treeNode.getProofNode();
    }
    
    public ProofTreeNode getProofTreeNode(ProofNode node, boolean allowExpansion) {
        ProofTreeNode ptroot = (ProofTreeNode)root;
        ProofTreeNode found= ptroot.findProofNode(node, allowExpansion);
        return found;
    }
    
    public TreePath getPath(ProofNode node, boolean allowExpansion) {
        ProofTreeNode found= getProofTreeNode(node, allowExpansion);
        
        if(found == null) {
            return null;
        } else {
            return found.getPath();
        }
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
        invalidateTree();
    }
    
    public void setShowNumbers(boolean b) {
        Log.enter(b);
        showNumbers = b;
        invalidateTree();
    }

    private void invalidateTree() {
        ProofTreeNode root = (ProofTreeNode) getRoot();
        root.invalidate();
        
        TreeModelEvent event = new TreeModelEvent(this, new TreePath(root));
        for(TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
            listener.treeStructureChanged(event);
        }
    }
    
    /*
     * instantiate the schema variables in a string.
     */
    private String instantiateString(RuleApplication ruleApp, String string) {
        TextInstantiator textInst = new TextInstantiator(ruleApp);
        return textInst.replaceInString(string, prettyPrint);
    }

    public void addChangedProofNode(ProofNode pn) {
        assert SwingUtilities.isEventDispatchThread();
        updateQueue.add(pn);
    }

    public void publishChanges() {
        assert SwingUtilities.isEventDispatchThread();
        
        ProofNode proofNode = updateQueue.poll();
        while(proofNode != null) {
            TreePath p = getPath(proofNode, false);

            if (p != null) {

                TreePath proofNodePath = p.getParentPath();
                if(proofNodePath == null)
                    proofNodePath = p;
                
                ProofTreeNode ptn = (ProofTreeNode) proofNodePath.getLastPathComponent();
                ptn.invalidate();
                ptn.expand();

                TreeModelEvent treeEvent = new TreeModelEvent(proofNode, proofNodePath);
                for(TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
                    listener.treeStructureChanged(treeEvent);
                }
            }
            
            proofNode = updateQueue.poll();
        }

    }

}
