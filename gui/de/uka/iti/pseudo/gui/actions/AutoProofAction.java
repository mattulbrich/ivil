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
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.proof.ProofNode;

/**
 * Creates a job, that tries to close all open goals with the current strategy.
 * 
 * @author felden@ira.uka.de
 */
public class AutoProofAction extends ParallelAutoProofAction {

    private static final long serialVersionUID = -7094117185284991811L;

    public AutoProofAction() {
        super("Automatic Proof");
        putValue(SHORT_DESCRIPTION, "Run automatic proving on all nodes");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK));
    }

    @Override
    public List<ProofNode> getInitialList() {
        return getProofCenter().getProof().getOpenGoals();
    }
}
