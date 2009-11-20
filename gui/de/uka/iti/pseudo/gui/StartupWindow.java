package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.uka.iti.pseudo.gui.bar.BarAction;
import de.uka.iti.pseudo.gui.bar.BarManager;

// TODO Documentation needed
public class StartupWindow extends JFrame {
    private BarManager barManager;
    
    public StartupWindow() throws IOException {
        super("Pseudo");
        makeGUI();
    }

    private void makeGUI() throws IOException {
        {
            URL resource = getClass().getResource("bar/menu.properties");
            if(resource == null)
                throw new IOException("resource bar/menu.properties not found");
            barManager = new BarManager(null, resource);
            barManager.putProperty(BarAction.PARENT_FRAME, this);
            setJMenuBar(barManager.makeMenubar("menubar.startup"));
        }
        getContentPane().add(new JLabel(makeImage()), BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }
    
    private Icon makeImage() {
        return BarManager.makeIcon(getClass().getResource("img/logo.png"));
    }
}
