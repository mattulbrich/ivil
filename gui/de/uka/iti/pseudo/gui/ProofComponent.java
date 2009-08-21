package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.awt.Font;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import nonnull.NonNull;
import nonnull.Nullable;

import de.uka.iti.pseudo.gui.ProofComponentModel.ProofTreeNode;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.Util;

/**
 * A proof component is a specialised JTree which is used to visualise proof trees.
 * 
 * <p>There is a specialised {@link Renderer} which is an extension to the {@link DefaultTreeCellRenderer}
 * and marks open leafs bold and branching points in italics. 
 *
 * <p>The model of the tree is a ProofComponentModel which maps a proof to a tree.
 * 
 * @see ProofComponentModel
 */
public class ProofComponent extends JTree implements ProofNodeSelectionListener {

    private static final long serialVersionUID = 6352175425195393727L;

    /*
     * some UI constants
     */
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
                
                setText(treenode.getLabel());
                if(!treenode.isLeaf()) {
                    setFont(italicFont);
                } else if(appliedRuleApp != null) {
                    setFont(null);
                } else {
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

    }

    /**
     * create a new proof component which shows the given proof.
     * 
     * @param proof to be displaed
     */
    public ProofComponent(@NonNull Proof proof) {
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
    
    /**
     * returns the proof node to which the currently selected item refers.
     * The result may be null if nothing is selected or the selection does
     * not belong to a proof node
     * @return the currently selected proof node
     */
    public @Nullable ProofNode getSelectedProofNode() {
        TreePath selectionPath = getSelectionPath();
        if(selectionPath != null)
            return proofModel.getProofNode(selectionPath);
        else
            return null;
    }
    
    /*
     * methods from the ProofNodeSelectionListener interface
     */
    public void proofNodeSelected(ProofNode node) {
        setSelectionPath(proofModel.getPath(node));
        repaint();
    }

    public void ruleApplicationSelected(RuleApplication ruleApplication) {
        // we do not care about rule applications.
    }


    protected ProofCenter getProofCenter() {
        return null;
    }

}
