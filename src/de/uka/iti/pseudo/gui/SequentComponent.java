package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

// TODO DOC

public class SequentComponent extends JPanel implements ProofNodeSelectionListener, PropertyChangeListener {
    
    private static final long serialVersionUID = -3882151273674917147L;
    
    private static int SEP_LENGTH = 32;
    private static int SEP_WIDTH = 6;
    private static int GAP = 3;
    
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
            Insets insets = parent.getInsets();
            int leftMargin = SEP_LENGTH/2 + insets.left;
            
            int h = insets.top;
            for(int i = 0; i < parent.getComponentCount(); i++) {
                Component comp = parent.getComponent(i);
                Dimension prefd = comp.getPreferredSize();
                if(comp instanceof Separator) {
                    comp.setLocation(insets.left, h);
                } else {
                    comp.setBounds(leftMargin, h, width-leftMargin - insets.right, prefd.height);
                }
                h += prefd.height + GAP;
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            int w = 0;
            int h = 0;
            for(int i = 0; i < parent.getComponentCount(); i++) {
                Dimension prefd = parent.getComponent(i).getMinimumSize();
                w = Math.max(w, prefd.width);
                h += prefd.height + GAP;
            }
            Insets insets = parent.getInsets();
            return new Dimension(w + SEP_LENGTH / 2 + insets.left + insets.right,
                    h-GAP + insets.top + insets.bottom);
        }

        public Dimension preferredLayoutSize(Container parent) {
            int w = 0;
            int h = 0;
            for(int i = 0; i < parent.getComponentCount(); i++) {
                Dimension prefd = parent.getComponent(i).getPreferredSize();
                w = Math.max(w, prefd.width);
                h += prefd.height + GAP;
            }
            Insets insets = parent.getInsets();
            return new Dimension(w + SEP_LENGTH / 2 + insets.left + insets.right,
                    h-GAP + insets.top + insets.bottom);
        }

    };
    
    private MouseListener termMouseListener = new MouseAdapter() {
        @Override 
        public void mouseClicked(MouseEvent e) {
            
            if(!SwingUtilities.isRightMouseButton(e) || e.getClickCount() > 1)
                return;
            
            // we listen only to TermComponents, this will not fail
            TermComponent tc = (TermComponent) e.getSource();
            
            TermSelector termSelector = tc.getTermAt(e.getPoint());
            fireRuleApp(termSelector);
        }  
    };

    private Separator separator = new Separator();
    private Sequent sequent;
    private Environment env;
    private PrettyPrint prettyPrinter;

    public SequentComponent(@NonNull Environment env) {
        this.env = env;
        this.setLayout(new Layout());
        prettyPrinter = new PrettyPrint(env, false, true);
        prettyPrinter.addPropertyChangeListener(this);
    }
    
    public void setSequent(Sequent sequent) {
        this.sequent = sequent;
        
        this.removeAll();
        
        int i = 0;
        for (Term t : sequent.getAntecedent()) {
            TermComponent termComp = new TermComponent(t, env, new TermSelector(TermSelector.ANTECEDENT, i), prettyPrinter);
            termComp.addMouseListener(termMouseListener);
            add(termComp);
            i++;
        }
        
        add(separator);
        
        i = 0;
        for (Term t : sequent.getSuccedent()) {
            TermComponent termComp = new TermComponent(t, env, new TermSelector(TermSelector.SUCCEDENT, i), prettyPrinter);
            termComp.addMouseListener(termMouseListener);
            add(termComp);
            i++;
        }
        
        doLayout();
        repaint();
    }
    
    private void fireRuleApp(TermSelector termSelector) {
        for(TermSelectionListener ral : listenerList.getListeners(TermSelectionListener.class)) {
            ral.termSelected(sequent, termSelector);
        }
    }
    
    public void addTermSelectionListener(TermSelectionListener obs) {
        listenerList.add(TermSelectionListener.class, obs);
    }

    public void removeTermSelectionListener(TermSelectionListener obs) {
        listenerList.remove(TermSelectionListener.class, obs);
    }

    public void proofNodeSelected(ProofNode node) {
        if(node.getChildren() == null)
            prettyPrinter.setInitialStyle(null);
        else
            prettyPrinter.setInitialStyle("closed");
        
        setSequent(node.getSequent());
        RuleApplication ruleApp = node.getAppliedRuleApp();
        if(ruleApp != null)
            ruleApplicationSelected(ruleApp);
    }

    public void markTerm(TermSelector selector, int type) {
        
        if(sequent == null)
            return;
        
        TermComponent termComp;
        if(selector.isAntecedent()) {
            termComp = (TermComponent) getComponent(selector.getTermNo()); 
        } else {
            // need to jump over antecedent and separator
            int offset = sequent.getAntecedent().size() + 1;
            termComp = (TermComponent) getComponent(offset + selector.getTermNo());
        }
        
        termComp.markSubterm(selector.getSubtermNo(), type);
    }

    public void ruleApplicationSelected(RuleApplication ruleApplication) {
        //
        // clear all previous markings on the term components
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (c instanceof TermComponent) {
                TermComponent termComponent = (TermComponent) c;
                termComponent.clearMarks();
            }
        }
        
        //
        // set the current markings
        markTerm(ruleApplication.getFindSelector(), 0);
        for (TermSelector sel : ruleApplication.getAssumeSelectors()) {
            markTerm(sel, 1);
        }
    }

    public PrettyPrint getPrettyPrinter() {
        return prettyPrinter;
    }

    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        // property on the pretty printer has changed --> remake the term components
        if(sequent != null && !evt.getPropertyName().equals(PrettyPrint.INITIALSTYLE_PROPERTY))
            setSequent(sequent);
    }
}