package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import de.uka.iti.pseudo.gui.Main;

public class TESTListRecentFiles extends BarAction {

    public TESTListRecentFiles() {
        super("TEST List recent files to stdout");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Preferences prefs = Preferences.userNodeForPackage( Main.class );
        String recent[] = prefs.get("recent files", "").split("\n");
        
        for (String string : recent) {
            System.out.println(string);
        }
    }

}
