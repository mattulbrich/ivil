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

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.gui.ProofComponentModel.ProofTreeNode;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.settings.Settings;

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
public class ProofComponent extends JTree implements PropertyChangeListener {
    
    private static final long serialVersionUID = 6352175425195393727L;

    public static final int DEFAULT_VERBOSITY = 
        Settings.getInstance().getInteger("pseudo.prooftree.defaultverbosity", 10);
    
    public static final boolean DEFAULT_SHOW_NUMBER =
        Settings.getInstance().getBoolean("pseudo.prooftree.shownumbers", false);
    
    /*
     * some UI constants
     */
    private static final Icon GREEN_ICON = mkIcon("img/green.png");
    private static final Icon GREY_ICON = mkIcon("img/grey.png");

    private static final String POPUP_BAR_PROPERTY = "proofComponent.popup";

    
    private final Font italicFont = getFont().deriveFont(Font.ITALIC);
    private final Font boldFont = getFont().deriveFont(Font.BOLD);
    
    private ProofComponentModel proofModel;
    
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
     * @param proofCenter to be displayed
     * @throws IOException if the bar manager fails to build the popup menu
     */
    public ProofComponent(@NonNull ProofCenter proofCenter) throws IOException {
        
        // this.proof = proof;
        Proof proof = proofCenter.getProof();
        proofModel = new ProofComponentModel(proof.getRoot(), proofCenter);
        proofCenter.addNotificationListener(ProofCenter.PROOFNODE_HAS_CHANGED, proofModel);
        proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, proofModel);
        setModel(proofModel);
        setCellRenderer(new Renderer());
        addListeners(proofCenter);
        proofCenter.firePropertyChange(ProofCenter.TREE_VERBOSITY, DEFAULT_VERBOSITY);
        proofCenter.firePropertyChange(ProofCenter.TREE_SHOW_NUMBERS, DEFAULT_SHOW_NUMBER);
        JPopupMenu popup = proofCenter.getBarManager().makePopup(POPUP_BAR_PROPERTY);
        addMouseListener(new TreePopupMouseListener(this, popup));
    }


    private void addListeners(final ProofCenter proofCenter) {
        proofCenter.addPropertyChangeListener(ProofCenter.TREE_VERBOSITY,
                new PropertyChangeListener() {
                    @Override public void propertyChange(PropertyChangeEvent evt) {
                        ProofNode currentProofNode = getSelectedProofNode();
                        proofModel.setVerbosity((Integer) evt.getNewValue());
                        if(currentProofNode != null)
                            setSelectionPath(proofModel.getPath(currentProofNode));
                        repaint();
                    }
                });
        
        proofCenter.addPropertyChangeListener(ProofCenter.TREE_SHOW_NUMBERS,
                new PropertyChangeListener() {
                    @Override public void propertyChange(PropertyChangeEvent evt) {
                        Log.enter(evt);
                        proofModel.setShowNumbers((Boolean) evt.getNewValue());
                        repaint();
                    }
                });
    }


    private static Icon mkIcon(String string) {
        URL resource = ProofComponent.class.getResource(string);
        return GUIUtil.makeIcon(resource);
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
    
    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            setSelectionPath(proofModel.getPath(node));
            repaint();
        }
    }
    
}