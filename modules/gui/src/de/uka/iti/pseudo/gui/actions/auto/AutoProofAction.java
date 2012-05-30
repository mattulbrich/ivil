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

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * Creates a job, that tries to close all open goals with the current strategy.
 * 
 * @author felden@ira.uka.de
 */
public class AutoProofAction extends ParallelAutoProofAction {

    private static Icon GO_ICON =
        GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    
    private static final long serialVersionUID = -7094117185284991811L;

    public AutoProofAction() {
        super("Automatic Proof");
        putValue(SHORT_DESCRIPTION, "Run automatic proving on all nodes");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
    }

    @Override
    public List<ProofNode> getInitialList() {
        return getProofCenter().getProof().getOpenGoals();
    }

    @Override
    protected Icon getGoIcon() {
        return GO_ICON;
    }
}
