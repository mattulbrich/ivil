package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.where.Interactive;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.PopupDisappearListener;
import de.uka.iti.pseudo.util.Triple;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.WindowMover;

public class InteractiveRuleApplicationComponent extends
        RuleApplicationComponent implements ActionListener, ListSelectionListener {

    public InteractiveRuleApplicationComponent(ProofCenter proofCenter, List<RuleApplication> ruleApps) {
        super(proofCenter);
        this.interactiveApplications = ruleApps;
        makeGUI();
    }

    private List<RuleApplication> interactiveApplications;
    private JPanel applicableListPanel;
    private JList applicableList;

    private void makeGUI() {
        applicableListPanel = new JPanel(new BorderLayout());
        applicableListPanel.setBorder(BorderFactory.createTitledBorder("Applicable rules"));
        applicableList = new JList(makeModel());
        applicableList.setEnabled(!interactiveApplications.isEmpty());
        applicableList.setBorder(BorderFactory.createEtchedBorder());
        applicableList.addListSelectionListener(this);
        applicableListPanel.add(applicableList, BorderLayout.CENTER);
        add(applicableListPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        // action listener for the poor
        applicableList.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                    actionPerformed(null);
            }
        });
    }

    public Vector<?> makeModel() {
        Vector<Object> applications = new Vector<Object>();
        if(interactiveApplications.isEmpty()) {
            applications.add("No applicable rules");
        } else {
            applications.addAll(interactiveApplications);
        }
        return applications;
    }
    
    public void ruleApplicationSelected(RuleApplication ruleApp) {
        this.setRuleApplication(ruleApp);
        invalidate();
    }
    
    /*
     * This method is called when a new value in the rule application
     * list is selected.
     */
    public void valueChanged(ListSelectionEvent e) {
        // ignore those "adjusting" events
        if(e.getValueIsAdjusting())
            return;
        
        Object selected = applicableList.getSelectedValue();
        if (selected instanceof ImmutableRuleApplication) {
            RuleApplication ruleApp = (RuleApplication) selected;
            proofCenter.fireSelectedRuleApplication(ruleApp);
        }
    }
    
    // chosen ruleappl
    public void actionPerformed(ActionEvent e) {
        Object selected = applicableList.getSelectedValue();
        if (selected instanceof ImmutableRuleApplication) {
            // TODO illegal instantiations
            try {
                
                MutableRuleApplication app = new MutableRuleApplication((RuleApplication) selected);
                
                // collect the user instantiations
                for (Triple<String, Type, ? extends JTextComponent> pair : interactionList) {
                    String varname = pair.fst();
                    Type type = pair.snd();
                    String content = pair.trd().getText();
                    
                    
                    Term term = TermMaker.makeAndTypeTerm(content, env, 
                            "User input for " + varname, type);
                    
                    assert type.equals(term.getType());
                    
                    app.getSchemaVariableMapping().put(varname, term);
                }
                putClientProperty("finished", true);
                proofCenter.apply(app);
            } catch (ProofException ex) {
                // TODO gescheite Fehlerausgabe
                ex.printStackTrace();
            } catch (ParseException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (ASTVisitException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
    }
    
    @Override protected void setInstantiations(RuleApplication app) {
        super.setInstantiations(app);
        
        for(Map.Entry<String, String> entry : app.getProperties().entrySet()) {
            String key = entry.getKey();
            if(!key.startsWith(Interactive.INTERACTION))
               continue;
            
            String svName = Util.stripQuotes(key.substring(Interactive.INTERACTION.length()));
            Type svType;
            try {
                svType = TermMaker.makeType(entry.getValue(), env);
            } catch (ASTVisitException e) {
                System.err.println("cannot parseType: " + entry.getValue() + ", continue anyway");
                continue;
            } catch (ParseException e) {
                System.err.println("cannot parseType: " + entry.getValue() + ", continue anyway");
                continue;
            }
            
            JLabel label = new JLabel(svName + " as " + svType);
            instantiationsPanel.add(label, 0);
            BracketMatchingTextArea textField = new BracketMatchingTextArea();
            textField.addActionListener(this);
            instantiationsPanel.add(textField, 1);
            interactionList.add(Triple.make(svName, svType, textField));
            instantiationsPanel.add(Box.createRigidArea(new Dimension(10,10)));
        }
    }

}


class InteractiveRuleApplicationPopup extends JWindow {

    public InteractiveRuleApplicationPopup(ProofCenter proofCenter,
            List<RuleApplication> ruleApps, Point location) {
        super(proofCenter.getMainWindow());
        
        RuleApplicationComponent rac = new InteractiveRuleApplicationComponent(proofCenter, ruleApps);
        proofCenter.addProofNodeSelectionListener(rac);
        
        JScrollPane sp = new JScrollPane(rac);
        
        setAlwaysOnTop(true);
        getContentPane().add(sp);
        setSize(300,500);
        setLocation(location);
        WindowMover windowMover = new WindowMover(this);
        sp.setBorder(windowMover);
        sp.addMouseListener(windowMover);
        sp.addMouseMotionListener(windowMover);
        Border bevelBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        sp.setBorder(BorderFactory.createCompoundBorder(bevelBorder, windowMover));
        new PopupDisappearListener(rac, this);
    }
}