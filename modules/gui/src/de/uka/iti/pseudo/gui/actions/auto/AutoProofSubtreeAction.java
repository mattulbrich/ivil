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
package de.uka.iti.pseudo.gui.actions.auto;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.proof.ProofNode;

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

    public AutoProofSubtreeAction() {
        super("Automatic Proof Subtree");
        putValue(SHORT_DESCRIPTION, "Run automatic proving on the current node");
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
}