package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.bar.StateListener.StateChangeEvent;

// TODO Documentation needed
public class StartupWindow extends JFrame {
    private BarManager barManager;
    
    public StartupWindow() throws IOException {
        super("Pseudo");
        makeGUI();
    }

    private void makeGUI() throws IOException {
        {
            barManager = new BarManager(null);
            barManager.putProperty(BarManager.PARENT_FRAME, this);
            URL resource = getClass().getResource("bar/startupmenu.properties");
            if(resource == null)
                throw new IOException("resource bar/startupmenu.properties not found");
            setJMenuBar(barManager.makeMenubar(resource));
        }
        getContentPane().add(new JLabel(makeImage()), BorderLayout.CENTER);
        pack();
    }
    
    private Icon makeImage() {
        return BarManager.makeIcon(getClass().getResource("img/pseudo.png"));
    }
}
