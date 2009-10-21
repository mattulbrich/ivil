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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.SequentHistory;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.proof.SequentHistory.Annotation;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.settings.Settings;

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
    
    private static class Layout implements LayoutManager {

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
    
    private Separator separator = new Separator();
    private ProofNode proofNode;
    private boolean open;
    private Environment env;
    private ProofCenter proofCenter;
    private PrettyPrint prettyPrinter;


    public SequentComponent(@NonNull ProofCenter proofCenter) {
        this.env = proofCenter.getEnvironment();
        this.proofCenter = proofCenter;
        this.setLayout(new Layout());
        prettyPrinter = proofCenter.getPrettyPrinter();
        prettyPrinter.addPropertyChangeListener(this);
        
        setBackground(Settings.getInstance().getColor("pseudo.sequentview.background"));
    }
    
    private void setProofNode(ProofNode proofNode, boolean open) {
        this.proofNode = proofNode;
        this.open = open;
        
        Sequent sequent = proofNode.getSequent();
        SequentHistory history = proofNode.getSequentHistory();
        
        this.removeAll();
        
        int i = 0;
        for (Term t : sequent.getAntecedent()) {
            TermSelector termSelector = new TermSelector(TermSelector.ANTECEDENT, i);
            Annotation annotation = history.select(termSelector);
            TermComponent termComp = new TermComponent(t, annotation, open, env, termSelector, prettyPrinter);
            termComp.addMouseListener(termMouseListener);
            termComp.setToolTipText("<html><a href=\"p3\">sdfsfd");
            add(termComp);
            i++;
        }
        
        add(separator);
        
        i = 0;
        for (Term t : sequent.getSuccedent()) {
            TermSelector termSelector = new TermSelector(TermSelector.SUCCEDENT, i);
            Annotation annotation = history.select(termSelector);
            TermComponent termComp = new TermComponent(t, annotation, open, env, termSelector, prettyPrinter);
            termComp.addMouseListener(termMouseListener);
            add(termComp);
            i++;
        }
        
        validate();
        repaint();
    }
    
    public void proofNodeSelected(ProofNode node) {
        
        setProofNode(node, node.getChildren() == null);
        RuleApplication ruleApp = node.getAppliedRuleApp();
        if(ruleApp != null)
            ruleApplicationSelected(ruleApp);
    }

    public void markTerm(TermSelector selector, int type) {
        
        assert selector != null;
        
        if(proofNode == null)
            return;
        
        TermComponent termComp;
        if(selector.isAntecedent()) {
            termComp = (TermComponent) getComponent(selector.getTermNo()); 
        } else {
            // need to jump over antecedent and separator
            int offset = proofNode.getSequent().getAntecedent().size() + 1;
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
        TermSelector findSelector = ruleApplication.getFindSelector();
        // might be a rule w/o find clause
        if(findSelector != null) {
            markTerm(findSelector, 0);
        }
        for (TermSelector sel : ruleApplication.getAssumeSelectors()) {
            markTerm(sel, 1);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // property on the pretty printer has changed --> remake the term components
        if(proofNode != null && !evt.getPropertyName().equals(PrettyPrint.INITIALSTYLE_PROPERTY))
            setProofNode(proofNode, open);
    }
    
    protected ProofCenter getProofCenter() {
        return null;
    }

    // the listener that launches and makes the rule popup
    private MouseListener termMouseListener = new MouseAdapter() {

        @Override 
        public void mouseClicked(MouseEvent e) {

            if(SwingUtilities.isLeftMouseButton(e)) {
                try {
                    TermComponent tc = (TermComponent) e.getSource();
                    TermSelector termSelector = tc.getTermAt(e.getPoint());
                    System.out.println("Mouse selected: " + termSelector);
                    
                    if(termSelector == null)
                        return;
                    
                    Sequent sequent = proofNode.getSequent();
                    List<RuleApplication> ruleApps = proofCenter.getApplicableRules(sequent, termSelector);

                    new InteractiveRuleApplicationPopup(proofCenter, ruleApps, 
                            e.getLocationOnScreen()).setVisible(true);

                } catch (ProofException ex) {
                    ExceptionDialog.showExceptionDialog(proofCenter.getMainWindow(), ex);
                }
            }
        }  
    };
}
