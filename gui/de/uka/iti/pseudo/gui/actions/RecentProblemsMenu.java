package de.uka.iti.pseudo.gui.actions;

import java.util.Date;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

// FIXME FOR DEMONSTRATION PURPOSES ONLY ... WILL BE CHANGED

public class RecentProblemsMenu extends JMenu implements MenuListener {
    
    public RecentProblemsMenu() {
        super("Recent files ...");
        add(new JMenuItem(new Date().toLocaleString()));
        addMenuListener(this);
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuSelected(MenuEvent e) {
        removeAll();
        add(new JMenuItem(new Date().toLocaleString()));
    }
    
    

}
