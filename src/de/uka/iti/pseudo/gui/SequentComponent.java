package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

public class SequentComponent extends JPanel {
    
    private static int SEP_LENGTH = 32;
    private static int SEP_WIDTH = 6;
    private static int GAP = 3;
    private static Color BACKGROUND = new Color(240, 240, 255);
    
    private static class Separator extends Component {
        
        public Separator() {
            setSize(SEP_LENGTH, SEP_LENGTH);
            setPreferredSize(getSize());
        }
        
        public void paint(Graphics g) {
            g.fillRect(0, 0, SEP_WIDTH, SEP_LENGTH);
            g.fillRect(0, (SEP_LENGTH-SEP_WIDTH)/2, SEP_LENGTH, SEP_WIDTH);
        }
    }
    
    private class Layout implements LayoutManager {

        public void addLayoutComponent(String name, Component comp) { }
        public void removeLayoutComponent(Component comp) { }

        public void layoutContainer(Container parent) {
            
            int width = parent.getWidth();
            
            int h = 0;
            for(int i = 0; i < parent.getComponentCount(); i++) {
                Component comp = parent.getComponent(i);
                Dimension prefd = comp.getPreferredSize();
                if(comp instanceof Separator) {
                    comp.setLocation(0, h);
                    comp.setSize(prefd);
                } else {
                    comp.setLocation(SEP_LENGTH / 2, h);
                    comp.setSize(width-SEP_LENGTH/2, prefd.height);
                }
                h += prefd.height + GAP;
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        public Dimension preferredLayoutSize(Container parent) {
            int w = 0;
            int h = 0;
            for(int i = 0; i < parent.getComponentCount(); i++) {
                Dimension prefd = parent.getComponent(i).getPreferredSize();
                w = Math.max(w, prefd.width);
                h += prefd.height + GAP;
            }
            return new Dimension(w + SEP_LENGTH / 2, h-GAP);
        }

    };

    private Separator separator = new Separator();
    private Sequent sequent;
    private Environment env;

    public SequentComponent(Environment env) {
        this.env = env;
        this.setLayout(new Layout());
        this.setBackground(BACKGROUND);
    }
    
    public void setSequent(Sequent sequent) {
        this.sequent = sequent;
        
        this.removeAll();
        
        for (Term t : sequent.getAntecedent()) {
            add(new TermComponent(t, env));
        }
        add(separator);
        for (Term t : sequent.getSuccedent()) {
            add(new TermComponent(t, env));
        }
    }
}