package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.editor.PFileEditor;

//TODO Documentation needed
@SuppressWarnings("serial") 
public class NewEditorAction extends BarAction {

    public NewEditorAction() {
        putValue(NAME, "New");
        putValue(SMALL_ICON, BarManager.makeIcon(NewEditorAction.class.getResource("img/page_white_text_new.png")));
        putValue(ACTION_COMMAND_KEY, "new");
        putValue(SHORT_DESCRIPTION, "Create and edit a new problem file");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        try {
            PFileEditor editor = new PFileEditor();
            editor.setSize(600, 800);
            Main.showFileEditor(editor);
        } catch (IOException e1) {
            // TODO gescheiter Fehler!
            e1.printStackTrace();
        }
    }
}