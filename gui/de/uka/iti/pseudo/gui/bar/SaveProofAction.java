/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import de.uka.iti.pseudo.gui.ExporterFileFilter;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.serialisation.ProofExport;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * This action is used to save a proof to a file. A dialog window pops up and
 * asks the user to select the file to store the proof to. All registered
 * implementing classes of {@link ProofExport} are listed as possible export
 * formats.
 */
@SuppressWarnings("serial") 
public class SaveProofAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    public SaveProofAction() {
        super("Save proof ...", GUIUtil.makeIcon(SaveProofAction.class.getResource("img/page_save.png")));
        putValue(ACTION_COMMAND_KEY, "saveProb");
        putValue(SHORT_DESCRIPTION, "save a proof to the currently active problem");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    }
    
    /*
     * Add myself as a listener to IN_PROOF messages. 
     */
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    /*
     * We are only listening to IN_PROOF messages so we can disable and enable
     * ourselves directly.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }
    
    /*
     * Show a save As Dialog and save the data using a {@link ProofExport}.
     */
    public void actionPerformed(ActionEvent e) {
        
        JFileChooser fileChooser = Main.makeFileChooser(Main.PROOF_FILE);
        
        while(true) {

            MainWindow mainWindow = getProofCenter().getMainWindow();
            int result = fileChooser.showSaveDialog(mainWindow);
            if(result == JFileChooser.APPROVE_OPTION) {

                FileFilter ff = fileChooser.getFileFilter();
                ProofExport proofExporter;
                if (ff instanceof ExporterFileFilter) {
                    ExporterFileFilter exportFilter = (ExporterFileFilter) ff;
                    proofExporter = exportFilter.getExporter();
                } else {
                    JOptionPane.showMessageDialog(mainWindow, "You need to choose a file format");
                    continue;
                }

                File selectedFile = fileChooser.getSelectedFile();
                if(selectedFile.exists()) {
                    result = JOptionPane.showConfirmDialog(mainWindow, "File " +
                            selectedFile + " exists. Overwrite?", 
                            "Overwrite file", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(result == JOptionPane.NO_OPTION)
                        continue;
                    if(result == JOptionPane.CANCEL_OPTION)
                        return;
                }

                FileOutputStream os = null;
                try {

                    os = new FileOutputStream(selectedFile);

                    Proof proof = getProofCenter().getProof();
                    proofExporter.exportProof(os, proof, 
                            getProofCenter().getEnvironment());
                    
                    proof.changesSaved();
                    
                } catch (Exception ex) {
                    ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
                } finally {
                    if(os != null)
                        try { os.close();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        }
                }
                return;
            } else {
                return;
            }
        }
    }
}
