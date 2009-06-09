package de.uka.iti.pseudo.gui;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

// TODO DOC

public class ProofComponentModel extends DefaultTreeModel implements Observer {
    
    private static final long serialVersionUID = -6525872309302128760L;
    
    private final static Vector<ProofTreeNode> EMPTY_VECTOR = new Vector<ProofTreeNode>();
    
    public static class ProofTreeNode implements TreeNode {
        
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
            
            children.add(new ProofTreeNode(this, node, true));
            while(nodeChildren != null && nodeChildren.size() == 1) {
                node = nodeChildren.get(0);
                nodeChildren = node.getChildren();
                children.add(new ProofTreeNode(this, node, true));
            }
            
            if(nodeChildren != null) {
                for (ProofNode pn : nodeChildren) {
                    children.add(new ProofTreeNode(this, pn, false));
                }
            }
        }

        public ProofNode getProofNode() {
            return proofNode;
        }

        public void invalidate() {
            children = null;
            label = null;
        }

        public String getLabel() {
            if(label == null) {
                if(!isLeaf()) {
                    label = getBranchName();
                } else {
                    RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();
                    if(appliedRuleApp != null) {
                        label = appliedRuleApp.getRule().getName();
                    } else {
                        label = "OPEN";
                    }
                }
            }
            return label;
        }

        private String getBranchName() {
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
            GoalAction ga = rule.getGoalActions()[index];
            String actionName = ga.getName();
            
            TermInstantiator termInst = new TermInstantiator(appliedRuleApp);
            
            return actionName == null ? "branch " + (index+1) : termInst.replaceInString(actionName);
        }
        
    }

    public ProofComponentModel(ProofNode root) {
        super(new ProofTreeNode(null, root, false));
    }

    public void update(Observable proof, Object arg) {
        
        ProofNode proofNode = (ProofNode) arg;
        TreePath path = getPath(proofNode).getParentPath();
        ProofTreeNode ptn =  (ProofTreeNode) path.getLastPathComponent();
        ptn.invalidate();
        
        TreeModelEvent event = new TreeModelEvent(proof, path);
        for(TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
            listener.treeStructureChanged(event);
        }
    }
    
    public ProofNode getProofNode(TreePath path) {
        ProofTreeNode treeNode = (ProofTreeNode) path.getLastPathComponent();
        return treeNode.proofNode;
    }

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

}
