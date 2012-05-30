/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * Tries to automatically close all nodes below the selected one with the
 * current strategy. If an inner node is selected, all children will be used.
 * 
 * @see AutoProofAction
 * 
 * @author felden@ira.uka.de
 * 
 */
@SuppressWarnings("serial")
public class AutoProofSubtreeAction extends ParallelAutoProofAction {
    
    private static Icon GO_ICON =
        GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_go_sub.png"));

    public AutoProofSubtreeAction() {
        super("Automatic Proof Subtree");
        putValue(SHORT_DESCRIPTION, "Run automatic proving on the current node");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
    }

    /**
     * Recursively searches for open nodes below current and adds them to list
     * 
     * @param current
     * @param list
     */
    private void addOpenChildGoals(ProofNode current, List<ProofNode> list) {
        if (null == current.getChildren())
            list.add(current);
        else
            for (ProofNode node : current.getChildren())
                addOpenChildGoals(node, list);
    }

    @Override
    public List<ProofNode> getInitialList() {
        List<ProofNode> rval = new LinkedList<ProofNode>();
        addOpenChildGoals(getProofCenter().getCurrentProofNode(), rval);
        return rval;
    }
    
    @Override
    protected Icon getGoIcon() {
        return GO_ICON;
    }
}