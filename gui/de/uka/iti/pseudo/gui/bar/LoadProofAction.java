package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.serialisation.ProofImport;
import de.uka.iti.pseudo.proof.serialisation.ProofXML;
import de.uka.iti.pseudo.util.ExceptionDialog;

// TODO Documentation needed

/*
 * have a look at SaveProofAction for generalisation with different file formats.
 */

@SuppressWarnings("serial") public class LoadProofAction extends BarAction
        implements PropertyChangeListener {

    private JFileChooser fileChooser;

    // at the moment there is only one, so hardcode it
    private ProofImport proofImport = new ProofXML();

    public LoadProofAction() {
        super("Load proof ...", BarManager.makeIcon(LoadProofAction.class
                .getResource("img/page.png")));
        putValue(ACTION_COMMAND_KEY, "loadProb");
        putValue(SHORT_DESCRIPTION,
                "load a proof to the currently active problem");
    }

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.PROPERTY_ONGOING_PROOF, this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

    public void actionPerformed(ActionEvent e) {

        Proof origProof = getProofCenter().getProof();
        if (origProof.getLock().tryLock()) {
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

                int result = fileChooser.showOpenDialog(getProofCenter()
                        .getMainWindow());
                if (result == JFileChooser.APPROVE_OPTION) {
                    Environment env = getProofCenter().getEnvironment();
                    FileInputStream is = new FileInputStream(fileChooser
                            .getSelectedFile());
                    if (!proofImport.acceptsInput(is))
                        throw new IOException("The input file "
                                + fileChooser.getSelectedFile()
                                + " is not accepted");

                    is = new FileInputStream(fileChooser.getSelectedFile());

                    proofImport.importProof(is, origProof, env);

                    // no unsaved changes now.
                    origProof.changesSaved();
                }

            } catch (Exception ex) {
                origProof.prune(origProof.getRoot());
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            } finally {
                origProof.getLock().unlock();
            }
        } else {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Cannot load proof, it is currently locked by another thread");
        }
    }
}
