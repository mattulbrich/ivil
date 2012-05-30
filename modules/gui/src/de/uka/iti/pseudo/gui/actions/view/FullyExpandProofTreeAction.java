/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.tree.TreeModel;

import de.uka.iti.pseudo.gui.ProofComponent;
import de.uka.iti.pseudo.gui.actions.BarAction;

public class FullyExpandProofTreeAction extends BarAction {
    
    private static final long serialVersionUID = -6907102062802798112L;

    public FullyExpandProofTreeAction() {
        super("Fully expand the proof tree");
    }

    @Override public void actionPerformed(ActionEvent e) {
        ProofComponent proofComponent = getProofCenter().getMainWindow().getProofComponent();
        TreeModel model = proofComponent.getModel();
        expandJTreeNode(proofComponent, model, model.getRoot(), 0, -1);
    }

    
    /**
     * Expands a given node in a JTree.
     * 
     * Thanks to:
     * {@link http://www.jguru.com/faq/view.jsp?EID=513951}
     *
     * @param tree      The JTree to expand.
     * @param model     The TreeModel for tree.     
     * @param node      The node within tree to expand.     
     * @param row       The displayed row in tree that represents
     *                  node.     
     * @param depth     The depth to which the tree should be expanded. 
     *                  Zero will just expand node, a negative
     *                  value will fully expand the tree, and a positive
     *                  value will recursively expand the tree to that
     *                  depth relative to node.
     */
    public static int expandJTreeNode (javax.swing.JTree tree,
                                       javax.swing.tree.TreeModel model,
                                       Object node, int row, int depth)
    {
        if (node != null  &&  !model.isLeaf(node)) {
            tree.expandRow(row);
            if (depth != 0)
            {
                for (int index = 0;
                     row + 1 < tree.getRowCount()  &&  
                                index < model.getChildCount(node);
                     index++)
                {
                    row++;
                    Object child = model.getChild(node, index);
                    if (child == null)
                        break;
                    javax.swing.tree.TreePath path;
                    while ((path = tree.getPathForRow(row)) != null  &&
                            path.getLastPathComponent() != child)
                        row++;
                    if (path == null)
                        break;
                    row = expandJTreeNode(tree, model, child, row, depth - 1);
                }
            }
        }
        return row;
    } // expandJTreeNode()

    
}
