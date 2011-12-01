package de.uka.iti.pseudo.gui.actions.editor;

import java.awt.event.ActionEvent;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.help.ReferenceManualWindow;
import de.uka.iti.pseudo.util.ExceptionDialog;

/**
 * This action launches the reference manual window.
 * It does not create a fresh instance every time.
 * 
 * @author mattias ulbrich
 */

@SuppressWarnings("serial")
public class ReferenceManualAction extends BarAction {

    private ReferenceManualWindow window;

    @Override
    public void actionPerformed(ActionEvent e) {
        
        try {
            if(window == null) {
                window = new ReferenceManualWindow();
            }
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
        
        
        window.setLocationRelativeTo(getParentFrame());
        window.setVisible(true);
    }
}
