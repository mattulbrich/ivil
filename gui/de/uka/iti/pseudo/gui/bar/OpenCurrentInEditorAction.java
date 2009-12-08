package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class OpenCurrentInEditorAction extends BarAction {
    
    public OpenCurrentInEditorAction() {
        putValue(NAME, "Edit current problem");
        putValue(SMALL_ICON, GUIUtil.makeIcon(OpenCurrentInEditorAction.class.getResource("img/link_edit.png")));
        putValue(SHORT_DESCRIPTION, "Open the currently active problem file in a new editor window");
    }
    
    public void actionPerformed(ActionEvent e) {
        
        File file = new File(getProofCenter().getEnvironment().getResourceName());

        try {
            Main.openEditor(file);
        } catch (IOException e1) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
        }
    }
}
