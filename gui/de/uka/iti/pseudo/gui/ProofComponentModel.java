/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.gui;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
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
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

/**
 * A proof component model is a specialised tree model for the ProofComponent
 *
 * @see ProofComponent
 */
public class ProofComponentModel extends DefaultTreeModel implements Observer {
    
    private static final long serialVersionUID = -6525872309302128760L;
    
    private final static Vector<ProofTreeNode> EMPTY_VECTOR = new Vector<ProofTreeNode>();
    
    private int verbosity;

    private PrettyPrint prettyPrint;
    
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

        public Enumeration<?> children() {
            expand();
            return children.elements();
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public TreeNode getChildAt(int childIndex) {
            expand();
            return children.elementAt(childIndex);
        }

        public int getChildCount() {
            expand();
            return children.size();
        }

        public int getIndex(TreeNode node) {
            expand();
            return children.indexOf(node);
        }

        public TreeNode getParent() {
            return parent;
        }

        public boolean isLeaf() {
            expand();
            return children.isEmpty();
        }

        private void expand() {
            if(children != null)
                return;
            
            children = new Vector<ProofTreeNode>();
            List<ProofNode> nodeChildren = proofNode.getChildren();
            ProofNode node = proofNode;
            
            if(shouldShow(node))
                children.add(new ProofTreeNode(this, node, true));
            
            while(nodeChildren != null && nodeChildren.size() == 1) {
                node = nodeChildren.get(0);
                nodeChildren = node.getChildren();
                if(shouldShow(node))
                    children.add(new ProofTreeNode(this, node, true));
            }
            
            if(nodeChildren != null) {
                for (ProofNode pn : nodeChildren) {
                    children.add(new ProofTreeNode(this, pn, false));
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

        public void invalidate() {
            children = null;
            label = null;
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
                    label = getBranchLabel();
                } else {
                    RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();
                    if(appliedRuleApp != null) {
                        // TODO possible instantiate a text.
                        label = getApplicationLabel();
                        // label = appliedRuleApp.getRule().getName();
                    } else {
                        label = "OPEN";
                    }
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
        private String getBranchLabel() {
            ProofNode parent = proofNode.getParent();
            
            if(parent == null)
                return "";
            
            int index = parent.getChildren().indexOf(proofNode);
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

        /*
         * instantiate the schema variables in a string.
         */
        private String instantiateString(RuleApplication ruleApp, String string) {
            Map<String, Term> schemaMap = ruleApp.getSchemaVariableMapping();
            Map<String, Type> typeMap = ruleApp.getTypeVariableMapping();
            Map<String, Update> updateMapping = ruleApp.getSchemaUpdateMapping();
            TermInstantiator termInst = new TermInstantiator(schemaMap, typeMap, updateMapping);
            
            return termInst.replaceInString(string, prettyPrint);
        }
        
    }

    public ProofComponentModel(ProofNode root, PrettyPrint pp) {
        // we cannot do this in one call, since this would give a comp. error
        super(null);
        setRoot(new ProofTreeNode(null, root, false));
        
        this.prettyPrint = pp;
    }

    /**
     * the observation is likely to appear outside the AWT thread
     */
    public void update(final Observable proof, final Object arg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProofNode proofNode = (ProofNode) arg;
                // assert proofNode.getAppliedRuleApp() != null;
                
                TreePath proofNodePath = getPath(proofNode);
                
                // in case the node is no longer seen we do not need to update
                // anything anyway.
                if (proofNodePath == null)
                    return;
                
                TreePath path = proofNodePath.getParentPath();
                ProofTreeNode ptn =  (ProofTreeNode) path.getLastPathComponent();
                ptn.invalidate();

                TreeModelEvent event = new TreeModelEvent(proof, path);
                for(TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
                    listener.treeStructureChanged(event);
                }
            }});
    }
    
    public ProofNode getProofNode(TreePath path) {
        ProofTreeNode treeNode = (ProofTreeNode) path.getLastPathComponent();
        return treeNode.proofNode;
    }

    /**
     * get the path to the node which corresponds to a certain proof node.
     * TODO is this depth first search? can we do better? do we need to?
     * @param node
     * @return
     */
    public TreePath getPath(ProofNode node) {
        LinkedList<Object> list = new LinkedList<Object>();
        if(findProofNode((ProofTreeNode) root, node, list)) {
            list.addFirst(root);
            return new TreePath(list.toArray());
        } else {
            return null;
        }
    }
    
    private boolean findProofNode(ProofTreeNode tree, ProofNode node, LinkedList<Object> path) {
        if(tree.isLeaf() && tree.proofNode == node) {
            return true;
        }
            
        tree.expand();
        for (ProofTreeNode ptn : tree.children) {
            if(findProofNode(ptn, node, path)) {
                path.addFirst(ptn);
                return true;
            }
        }
        
        return false;
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
        ProofTreeNode root = (ProofTreeNode) getRoot();
        root.invalidate();
        
        TreeModelEvent event = new TreeModelEvent(this, new TreePath(root));
        for(TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
            listener.treeStructureChanged(event);
        }
    }

}
