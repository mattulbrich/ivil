package de.uka.iti.pseudo.gui;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

public class ProofComponent extends JTree {

    private Proof proof;

    private class Model implements TreeModel, Observer {

        /** Listeners. */
        protected EventListenerList listenerList = new EventListenerList();

        public void addTreeModelListener(TreeModelListener l) {
            listenerList.add(TreeModelListener.class, l);
        }

        public void removeTreeModelListener(TreeModelListener l) {
            listenerList.remove(TreeModelListener.class, l);
        }

        public Object getChild(Object parent, int index) {
            ProofNode node = (ProofNode) parent;
            return node.getChildren().get(index);
        }

        public int getChildCount(Object parent) {
            ProofNode node = (ProofNode) parent;
            List<ProofNode> children = node.getChildren();
            if (children != null)
                return children.size();
            else
                return 0;
        }

        public int getIndexOfChild(Object parent, Object child) {
            ProofNode node = (ProofNode) parent;
            List<ProofNode> children = node.getChildren();
            if (children != null)
                return children.indexOf(child);
            else
                return -1;
        }

        public Object getRoot() {
            return proof.getRoot();
        }

        public boolean isLeaf(Object parent) {
            ProofNode node = (ProofNode) parent;
            List<ProofNode> children = node.getChildren();
            return children == null;
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
            throw new UnsupportedOperationException(
                    "The tree nodes must not be altered");
        }

        public void update(Observable proof, Object proofNode) {
            LinkedList<Object> path = new LinkedList<Object>();
            ProofNode node = (ProofNode) proofNode;
            while(node != null) {
                path.addFirst(node);
                node = node.getParent();
            }
            
            TreeModelEvent event = new TreeModelEvent(proof, path.toArray());
            for(TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
                listener.treeNodesChanged(event);
            }
        }

    }

    public ProofComponent(Proof proof) {
        this.proof = proof;
        Model model = new Model();
        proof.addChangeObserver(model);
        setModel(model);
        // DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        // Icon innerIcon = mkIcon("img/inner.png");
        // renderer.setIcon(innerIcon);
        // Icon outerIcon = mkIcon("img/outer.png");
        // renderer.setLeafIcon(outerIcon);
        // setCellRenderer(renderer);
    }

    private Icon mkIcon(String string) {
        URL resource = getClass().getResource(string);
        if (resource != null)
            return new ImageIcon(resource);
        else
            return null;
    }
    
    public String convertValueToText(Object value, boolean selected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        ProofNode node = (ProofNode) value;
        RuleApplication appliedRuleApp = node.getAppliedRuleApp();
        if(appliedRuleApp != null) {
            return appliedRuleApp.getRule().getName();
        } else {
            return "OPEN";
        }
    }

}
