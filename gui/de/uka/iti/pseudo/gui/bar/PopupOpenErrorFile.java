package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;

@SuppressWarnings("serial") 
public class PopupOpenErrorFile 
    extends BarAction 
    implements InitialisingAction, PropertyChangeListener{

    private String errorFile;

    public PopupOpenErrorFile() {
        super("Open erreroneus file");
        putValue(SHORT_DESCRIPTION, "open the file containing the error in a new window");
    }
    
    public void actionPerformed(ActionEvent e) {
        if(errorFile == null)
            return;
        try {
            File file = new File(errorFile); 
            PFileEditor editor = new PFileEditor(file);
            editor.setSize(600, 800);
            Main.showFileEditor(editor);
        } catch (IOException e1) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
        }
    }

    public void initialised() {
        getEditor().addPropertyChangeListener("errorFile", this);
        setEnabled(false);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        errorFile = (String) evt.getNewValue();
        setEnabled(errorFile != null);
    }

}
