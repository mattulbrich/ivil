/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.io;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.util.BufferOutputStream;
import de.uka.iti.pseudo.proof.serialisation.ProofXML;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * This action loads the url of the currently open proof center into a new frame
 * and tries to replay (step-by-step) the proof of the current frame in the new one.
 *
 * @author mattias ulbrich
 */

public class ReloadAndReproveProblemAction extends BarAction implements
		PropertyChangeListener {

    private static final long serialVersionUID = 2813296178193693367L;

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        ProofCenter proofCenter1 = getProofCenter();
        assert proofCenter1 != null : "ProofCenter must be available";

        BufferOutputStream buffer = null;
        try {
            URL url = new URL(proofCenter1.getEnvironment().getResourceName());

            // We use this instance for import and export;
            ProofXML proofXML = new ProofXML();
            buffer = new BufferOutputStream();

            proofXML.exportProof(buffer, proofCenter1.getProof(),
                    proofCenter1.getEnvironment());

            ProofCenter proofCenter2 = Main.openProverFromURL(url);
            if(proofCenter2 != null) {
                proofXML.importProof(buffer.inputStream(), proofCenter2.getProof(),
                        proofCenter2.getEnvironment(), null);
                proofCenter2.fireNotification(ProofCenter.PROOFTREE_HAS_CHANGED);
            }
        } catch (Exception ex) {
            String tmp = "<no file>";
            try {
                 tmp = File.createTempFile("ivilReloadProof", ".pxml").getAbsolutePath();
                 FileOutputStream fos = new FileOutputStream(tmp);
                 GUIUtil.drainStream(buffer.inputStream(), fos);
            } catch (Exception ex2) {
                tmp = "<no file>";
                ex2.printStackTrace();
            }

            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Something went wrong while replaying the proof. Saved as " + tmp, ex);
        }


    }
}
