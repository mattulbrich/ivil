package de.uka.iti.pseudo.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class ActionSet {

    static Action a = new AbstractAction() {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Hello World! " + getValue(SELECTED_KEY));
        }
    };
    
    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        
        a.putValue(Action.NAME, "NAME");
        JFrame f = new JFrame();
//        JToolBar tb = new JToolBar();
//        tb.add(new JToggleButton(a));
//        f.getContentPane().add(tb);
        JMenuBar mb = new JMenuBar();
        mb.add(new JCheckBoxMenuItem(a));
        f.setJMenuBar(mb);
        f.pack();
        f.setVisible(true);
        
        boolean b = true;
        while(true) {
            Thread.sleep(5000);
            a.putValue(Action.SELECTED_KEY, b);
        b = !b;
        }
    }

}
