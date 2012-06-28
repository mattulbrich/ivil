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

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.gui.ProofComponentModel.ProofTreeNode;
import de.uka.iti.pseudo.gui.sequent.InteractiveRuleApplicationComponent;
import de.uka.iti.pseudo.gui.util.OverlayIcon;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * A proof component is a specialised JTree which is used to visualise proof
 * trees.
 *
 * <p>
 * There is a specialised {@link Renderer} which is an extension to the
 * {@link DefaultTreeCellRenderer} and marks open leafs bold and branching
 * points in italics.
 *
 * <p>
 * The model of the tree is a {@link ProofComponentModel} which maps a proof to a tree.
 *
 * @see ProofComponentModel
 */
public class ProofComponent extends JTree {

    private static final long serialVersionUID = 6352175425195393727L;

    /*
     * some user constants
     */
    public static final int DEFAULT_VERBOSITY = Settings.getInstance()
            .getInteger("pseudo.prooftree.defaultverbosity", 10);

    public static final boolean DEFAULT_SHOW_NUMBER = Settings.getInstance()
            .getBoolean("pseudo.prooftree.shownumbers", false);

    /*
     * some UI constants
     */
    private static final Icon GREEN_ICON = mkIcon("img/green.png");
    private static final Icon GREY_ICON = mkIcon("img/grey.png");
    private static final Icon MANUAL_ICON = mkIcon("img/bullet_star.png");

    private final Font italicFont = getFont().deriveFont(Font.ITALIC);
    private final Font boldFont = getFont().deriveFont(Font.BOLD);

    private static final String POPUP_BAR_PROPERTY = "proofComponent.popup";

    /**
     * the model which maps the proofTree to swing elements.
     */
    private final ProofComponentModel proofModel;

    @SuppressWarnings("serial")
    private class Renderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            if (value instanceof ProofTreeNode) {
                ProofTreeNode treenode = (ProofTreeNode) value;
                ProofNode proofNode = treenode.getProofNode();
                RuleApplication appliedRuleApp = proofNode.getAppliedRuleApp();

                setText(treenode.getLabel());
                if (!treenode.isLeaf()) {
                    setFont(italicFont);
                } else if (appliedRuleApp != null) {
                    setFont(null);
                } else {
                    setFont(boldFont);
                }


                if (treenode.getParent() == null) {
                    setIcon(null);
                } else {
                    Icon icon;
                    if (proofNode.isClosed()) {
                        icon = GREEN_ICON;
                    } else {
                        icon = GREY_ICON;
                    }

                    icon = possibleOverlayManualIcon(treenode, icon);

                    setIcon(icon);
                }

                setSize(200, getHeight());
            }
            return this;
        }

        private Icon possibleOverlayManualIcon(ProofTreeNode treenode, Icon icon) {
            RuleApplication ra = treenode.getProofNode().getAppliedRuleApp();
            if(ra != null) {
                boolean manual = ra.getProperties().containsKey(
                        InteractiveRuleApplicationComponent.MANUAL_RULEAPP);

                if(manual) {
                    return new OverlayIcon(icon, MANUAL_ICON);
                }
            }
            return icon;
        }

    }

    /**
     * create a new proof component which shows the given proof.
     *
     * @param proofCenter
     *            to be displayed
     * @throws IOException
     *             if the bar manager fails to build the popup menu
     */
    public ProofComponent(@NonNull ProofCenter proofCenter) throws IOException {

        // this.proof = proof;
        Proof proof = proofCenter.getProof();
        proofModel = new ProofComponentModel(proof.getRoot(), proofCenter);
        setModel(proofModel);
        // large models allow changes in width of labels.
        setLargeModel(true);
        setCellRenderer(new Renderer());
        addListeners(proofCenter);
        proofCenter.firePropertyChange(ProofCenter.TREE_VERBOSITY,
                DEFAULT_VERBOSITY);
        proofCenter.firePropertyChange(ProofCenter.TREE_SHOW_NUMBERS,
                DEFAULT_SHOW_NUMBER);
        JPopupMenu popup = proofCenter.getBarManager().makePopup(
                POPUP_BAR_PROPERTY);
        addMouseListener(new TreePopupMouseListener(this, popup));
    }

    private void addListeners(final ProofCenter proofCenter) {
        proofCenter.addPropertyChangeListener(ProofCenter.TREE_VERBOSITY,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        ProofNode currentProofNode = getSelectedProofNode();
                        proofModel.setVerbosity((Integer) evt.getNewValue());
                        if (currentProofNode != null) {
                            setSelectionPath(proofModel.getPath(
                                    currentProofNode, true));
                        }
                        repaint();
                    }
                });

        proofCenter.addPropertyChangeListener(ProofCenter.TREE_SHOW_NUMBERS,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        Log.enter(evt);
                        proofModel.setShowNumbers((Boolean) evt.getNewValue());
                        // create a new cell render to trigger recalculation of label sizes
                        setCellRenderer(new Renderer());
                        repaint();
                    }
                });

        proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        ProofNode node = (ProofNode) evt.getNewValue();
                        TreePath path = proofModel.getPath(node, true);
                        Log.log(Log.VERBOSE, "Selecting " + node
                                + " in tree; path=" + path);
                        setSelectionPath(path);
                        repaint();
                    }
                });

        proofCenter.addNotificationListener(ProofCenter.PROOFNODES_HAVE_CHANGED, new NotificationListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleNotification(NotificationEvent event) {
                assert event.countParameters() == 1;
                List<ProofNode> pnodes = (List<ProofNode>) event.getParameter(0);
//                for (ProofNode pn : pnodes) {
//                    proofModel.addChangedProofNode(pn);
//                }

                proofModel.publishChanges(pnodes);

                // if no node was selected, select root
                if (selectionModel.isSelectionEmpty() && !(Boolean) proofCenter.getProperty(ProofCenter.ONGOING_PROOF)) {
                    selectionModel.setSelectionPath(proofModel.getPath(proofCenter.getCurrentProofNode(), true));
                }
                repaint();
            }
        });

        // proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED,
        // new NotificationListener() {
        // public void handleNotification(NotificationEvent event) {
        // ProofNode selection = getSelectedProofNode();
        //
        // // update the model
        // proofModel.publishChanges();
        //
        // // try to reestablish the selection!
        // // ProofTreeNode proofTreeNode =
        // proofModel.getProofTreeNode(selection, false);
        // // if(proofTreeNode != null) {
        // // TreePath path = proofTreeNode.getPath();
        // // Log.log(Log.VERBOSE, "Reselecting " + selection
        // // + " in tree; path=" + path);
        // // if (path != null) {
        // // setSelectionPath(path);
        // // }
        // // }
        // repaint();
        // }
        // });

    }

    private static Icon mkIcon(String string) {
        URL resource = ProofComponent.class.getResource(string);
        return GUIUtil.makeIcon(resource);
    }

    /**
     * returns the proof node to which the currently selected item refers. The
     * result may be null if nothing is selected or the selection does not
     * belong to a proof node
     *
     * @return the currently selected proof node
     */
    public @Nullable
    ProofNode getSelectedProofNode() {
        TreePath selectionPath = getSelectionPath();
        if (selectionPath != null) {
            return proofModel.getProofNode(selectionPath);
        } else {
            return null;
        }
    }

}