package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.serialisation.ProofExport;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveProofAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    private static class ExporterFileFilter extends FileFilter {
        
        ProofExport exporter;

        public ExporterFileFilter(ProofExport exporter) {
            this.exporter = exporter;
        }

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith("." + exporter.getFileExtension());
        }

        public String getDescription() {
            return exporter.getName();
        }
        
    }

    private JFileChooser fileChooser;
    
    private List<FileFilter> filters = new ArrayList<FileFilter>();
    
    public SaveProofAction() {
        super("Save proof ...", BarManager.makeIcon(SaveProofAction.class.getResource("img/page_save.png")));
        putValue(ACTION_COMMAND_KEY, "saveProb");
        putValue(SHORT_DESCRIPTION, "save a proof to the currently active problem");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        
        for(ProofExport export : ServiceLoader.load(ProofExport.class)) {
            filters.add(new ExporterFileFilter(export));
        }
    }
    
    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean)evt.getOldValue());
    }
    
    public void actionPerformed(ActionEvent e) {
        
        if(fileChooser == null) {
            fileChooser = new JFileChooser(".");
            for (FileFilter ff : filters) {
                fileChooser.addChoosableFileFilter(ff);
            }
        }
        
        while(true) {

            MainWindow mainWindow = getProofCenter().getMainWindow();
            int result = fileChooser.showSaveDialog(mainWindow);
            if(result == JFileChooser.APPROVE_OPTION) {

                FileFilter ff = fileChooser.getFileFilter();
                ProofExport proofExporter;
                if (ff instanceof ExporterFileFilter) {
                    ExporterFileFilter exportFilter = (ExporterFileFilter) ff;
                    proofExporter = exportFilter.exporter;
                } else {
                    // XXX
                    JOptionPane.showMessageDialog(mainWindow, "You need to choose file format");
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
                    // TODO gescheiter Fehlerdialog
                    ex.printStackTrace();
                } finally {
                    if(os != null)
                        try { os.close();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        }
                }
                return;
            }
        }
    }

}
