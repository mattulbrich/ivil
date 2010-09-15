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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofDaemon.Job;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.serialisation.ProofImport;
import de.uka.iti.pseudo.proof.serialisation.ProofXML;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed

/*
 * have a look at SaveProofAction for generalization with different file formats.
 */

@SuppressWarnings("serial") public class LoadProofAction extends BarAction
        implements InitialisingAction, PropertyChangeListener {

    private JFileChooser fileChooser;

    // at the moment there is only one, so hard code it
    private ProofImport proofImport = new ProofXML();

    public LoadProofAction() {
        super("Load proof ...", GUIUtil.makeIcon(LoadProofAction.class
                .getResource("img/page.png")));
        putValue(ACTION_COMMAND_KEY, "loadProb");
        putValue(SHORT_DESCRIPTION,
                "load a proof to the currently active problem");
    }

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

    public void actionPerformed(ActionEvent e) {
        final Proof origProof = getProofCenter().getProof();

        origProof.getDaemon().addJob(new Runnable() {
            public void run() {
                try {
                    List<ProofNode> rootChildren = origProof.getRoot()
                            .getChildren();
                    if (rootChildren != null) {
                        ExceptionDialog
                                .showExceptionDialog(getParentFrame(),
                                        "Root must not have children if loading a proof");
                    }

                    if (fileChooser == null)
                        fileChooser = new JFileChooser(".");

                    int result = fileChooser
                            .showOpenDialog(getProofCenter()
                                    .getMainWindow());
                    if (result == JFileChooser.APPROVE_OPTION) {
                        Environment env = getProofCenter().getEnvironment();
                        FileInputStream is = new FileInputStream(
                                fileChooser.getSelectedFile());
                        if (!proofImport.acceptsInput(is))
                            throw new IOException("The input file "
                                    + fileChooser.getSelectedFile()
                                    + " is not accepted");

                        is = new FileInputStream(fileChooser
                                .getSelectedFile());

                        proofImport.importProof(is, origProof, env);

                        // no unsaved changes now.
                        origProof.changesSaved();
                    }

                } catch (Exception ex) {
                    try {
                        getProofCenter().prune(
                                getProofCenter().getProof().getRoot());
                    } catch (ProofException ex2) {
                        ex2.printStackTrace();
                    }
                    ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
                }
            }
        });
    }
}
