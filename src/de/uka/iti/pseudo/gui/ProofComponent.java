package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.awt.Font;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import de.uka.iti.pseudo.gui.ProofComponentModel.ProofTreeNode;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.util.Util;
//TODO DOC
public class ProofComponent extends JTree implements ProofNodeSelectionListener {

    private static final long serialVersionUID = 6352175425195393727L;
    
    // private Proof proof;
    private ProofComponentModel proofModel;
    private static final Icon GREEN_ICON = mkIcon("img/green.png");
    private static final Icon GREY_ICON = mkIcon("img/grey.png");
    private final Font italicFont = getFont().deriveFont(Font.ITALIC);
    private final Font boldFont = getFont().deriveFont(Font.BOLD);
    
    @SuppressWarnings("serial") 
    private class Renderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof ProofTreeNode) {
                ProofTreeNode treenode = (ProofTreeNode) value;
                ProofNode proofNode = treenode.getProofNode();
                RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();
                
                if(!treenode.isLeaf()) {
                    setText(getBranchName(proofNode));
                    setFont(italicFont);
                } else if(appliedRuleApp != null) {
                    setText(appliedRuleApp.getRule().getName());
                    setFont(null);
                } else {
                    setText("OPEN");
                    setFont(boldFont);
                }
                
                if(treenode.getParent() == null) {
                    setIcon(null);
                } else if(proofNode.isClosed()) {
                    setIcon(GREEN_ICON);
                } else {
                    setIcon(GREY_ICON);
                }
            }
            return this;
        }

        private String getBranchName(ProofNode proofNode) {
            ProofNode parent = proofNode.getParent();
            
            if(parent == null)
                return "";
            
            int index = parent.getChildren().indexOf(proofNode);
            assert index != -1;
            
            RuleApplication appliedRuleApp = parent.getAppliedRuleApp();
            if(appliedRuleApp == null)
                return "branch " + (index+1);
            
            Rule rule = appliedRuleApp.getRule();
            GoalAction ga = rule.getGoalActions()[index];
            String actionName = ga.getName();
            
            return actionName == null ? "branch " + (index+1) : actionName;
        }
        
    }

    public ProofComponent(Proof proof) {
        // this.proof = proof;
        proofModel = new ProofComponentModel(proof.getRoot());
        proof.addObserver(proofModel);
        setModel(proofModel);
        setCellRenderer(new Renderer());
    }

    private static Icon mkIcon(String string) {
        URL resource = ProofComponent.class.getResource(string);
        if (resource != null)
            return new ImageIcon(resource);
        else
            return Util.UNKNOWN_ICON;
        }
    
    public void proofNodeSelected(ProofNode node) {
        setSelectionPath(proofModel.getPath(node));
        repaint();
    }

    public ProofNode getSelectedProofNode() {
        TreePath selectionPath = getSelectionPath();
        if(selectionPath != null)
            return proofModel.getProofNode(selectionPath);
        else
            return null;
    }

}
