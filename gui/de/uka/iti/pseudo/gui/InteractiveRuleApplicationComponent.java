/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
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
import java.beans.PropertyChangeEvent;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.gui.editor.CurlyHighlightPainter;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.where.Interactive;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.PopupDisappearListener;
import de.uka.iti.pseudo.util.Triple;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.WindowMover;
import de.uka.iti.pseudo.util.settings.ColorResolver;

/**
 * This class describes the component which is in the rule application popup.
 * 
 * <p>In addition to a normal RuleApplicationComponent, it possesses also a list
 * of rule application from which one can choose. The instantiations may 
 * also contain input fields for interactive instantiation.
 * 
 * <p>Once a rule application has been chosen it is sent to the attached 
 * ProofCenter to be applied via {@link ProofCenter#apply(RuleApplication)}. 
 */
public class InteractiveRuleApplicationComponent extends
        RuleApplicationComponent implements ActionListener, ListSelectionListener {

    private static final long serialVersionUID = 1198343020746961376L;

    public InteractiveRuleApplicationComponent(ProofCenter proofCenter, List<RuleApplication> ruleApps) {
        super(proofCenter);
        this.interactiveApplications = ruleApps;
        makeGUI();
    }

    // the applications to choose from
    private List<RuleApplication> interactiveApplications;
    
    // the panel that holds the list of applicable rules
    private JPanel applicableListPanel;
    
    // the list of applicable rules
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
            proofCenter.firePropertyChange(ProofCenter.SELECTED_RULEAPPLICATION, ruleApp);
        }
    }
    
    // chosen ruleappl
    public void actionPerformed(ActionEvent e) {
        Object selected = applicableList.getSelectedValue();
        if (selected instanceof ImmutableRuleApplication) {

            // remove old error notices
            for (Triple<String, Type, ? extends JTextComponent> pair : interactionList) {
                pair.trd().setBackground(getBackground());
                pair.trd().setToolTipText(null);
            }
            
            JTextComponent component = null;
            try {
                
                MutableRuleApplication app = new MutableRuleApplication((RuleApplication) selected);
                
                // collect the user instantiations
                for (Triple<String, Type, ? extends JTextComponent> pair : interactionList) {
                    String varname = pair.fst();
                    Type type = pair.snd();
                    component = pair.trd();
                    String content = component.getText();
                    
                    Term term = TermMaker.makeAndTypeTerm(content, env, 
                            "User input for " + varname, type);
                    
                    assert type.equals(term.getType());
                    
                    app.getSchemaVariableMapping().put(varname, term);
                }
                putClientProperty("finished", true);
                ProofNode next = proofCenter.apply(app);
                proofCenter.fireSelectedProofNode(next);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                if(component != null) {
                    component.setBackground(ColorResolver.getInstance().resolve("orange red"));
                    component.setToolTipText(htmlize(ex.getMessage()));
                } else {
                    System.err.println("We do not have a component to feed back to ...");
                    // TODO give feedback somewhere else
                }
            }
        }
    }
    
    private String htmlize(String message) {
        message = GUIUtil.htmlentities(message).replace("\n", "<br>");
        return "<html><pre>" + message + "</pre>";
    }
    
    /*
     * Super class reacts only to proof node selections, we react to 
     * rule application selection also. 
     */
    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        
        if(ProofCenter.SELECTED_RULEAPPLICATION.equals(evt.getPropertyName())) {
            RuleApplication ruleApp = (RuleApplication) evt.getNewValue();
            this.setRuleApplication(ruleApp);
            invalidate();
        }
    }
    
    @Override 
    protected void setInstantiations(RuleApplication app) {
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

/**
 * A little helper class which embeds a {@link InteractiveRuleApplicationComponent} 
 * into a {@link JWindow} which can then be shown as a popup.  
 */

@SuppressWarnings("serial") 
class InteractiveRuleApplicationPopup extends JWindow {

    private static final long serialVersionUID = 251617174624191216L;

    public InteractiveRuleApplicationPopup(ProofCenter proofCenter,
            List<RuleApplication> ruleApps, Point location) {
        super(proofCenter.getMainWindow());
        
        RuleApplicationComponent rac = new InteractiveRuleApplicationComponent(proofCenter, ruleApps);
        proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, rac);
        proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_RULEAPPLICATION, rac);
        
        JScrollPane sp = new JScrollPane(rac);
        
        // setAlwaysOnTop(true);
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