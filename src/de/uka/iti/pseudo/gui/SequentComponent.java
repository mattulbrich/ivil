package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.DeferredObservable;

public class SequentComponent extends JPanel {
    
    private static final long serialVersionUID = -3882151273674917147L;
    
    private static int SEP_LENGTH = 32;
    private static int SEP_WIDTH = 6;
    private static int GAP = 3;
    private static Color BACKGROUND = new Color(240, 240, 255);
    
    private static class Separator extends Component {
        
        private static final long serialVersionUID = -3610640407936158831L;

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
    
    private MouseListener termMouseListener = new MouseAdapter() {
        @Override 
        public void mouseClicked(MouseEvent e) {
            
            if(!SwingUtilities.isRightMouseButton(e) || e.getClickCount() > 1)
                return;
            
            // we listen only to TermComponents, this will not fail
            TermComponent tc = (TermComponent) e.getSource();
            
            showRulePopup(e.getPoint(), tc.getTermAt(e.getPoint()));
        }  
    };

    private Separator separator = new Separator();
    private Sequent sequent;
    private Environment env;
    private Observable ruleApplicationObservable = new DeferredObservable();

    public SequentComponent(Environment env) {
        this.env = env;
        this.setLayout(new Layout());
        this.setBackground(BACKGROUND);
    }
    
    public void setSequent(Sequent sequent) {
        this.sequent = sequent;
        
        this.removeAll();
        
        int i = 0;
        for (Term t : sequent.getAntecedent()) {
            TermComponent termComp = new TermComponent(t, env, new TermSelector(TermSelector.ANTECEDENT, i));
            termComp.addMouseListener(termMouseListener);
            add(termComp);
            i++;
        }
        
        add(separator);
        
        i = 0;
        for (Term t : sequent.getSuccedent()) {
            TermComponent termComp = new TermComponent(t, env, new TermSelector(TermSelector.SUCCEDENT, i));
            termComp.addMouseListener(termMouseListener);
            add(termComp);
            i++;
        }
    }
    
    private void showRulePopup(Point p, TermSelector termSelector) {
        RuleApplicationFinder raf = new RuleApplicationFinder(sequent, termSelector, env);
        List<RuleApplication> ruleAppList = raf.findAll();
        
        JPopupMenu menu = new JPopupMenu();
        for (final RuleApplication ruleApp : ruleAppList) {
            JMenuItem item = new JMenuItem(ruleApp.getRule().getName());
            // TODO
            item.setToolTipText("to be done");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireRuleApp(ruleApp);
                } });
        }
    }
    
    private void fireRuleApp(RuleApplication ruleApp) {
        ruleApplicationObservable.notifyObservers(ruleApp);
    }
    
    public void addRuleApplicationObserver(Observer obs) {
        ruleApplicationObservable.addObserver(obs);
    }

    public void removeRuleApplicationObserver(Observer obs) {
        ruleApplicationObservable.deleteObserver(obs);
    }
}