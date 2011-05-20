/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.sequent.InteractiveRuleApplicationComponent;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * A component to display a rule application.
 * 
 * It allows for extension!
 * 
 * @see InteractiveRuleApplicationComponent
 */
@SuppressWarnings("serial") 

public class RuleApplicationComponent extends JPanel implements PropertyChangeListener, NotificationListener {
    
    /**
     * The font to use for printing rules
     */
    private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);

    /**
     * The rule application to display
     */
    private RuleApplication ruleApplication;

    /**
     * The underlying environment.
     */
    protected Environment env;

    /**
     * The text area to print the rule text.
     */
    private JTextArea ruleText;

    /**
     * The instantiations panel. Is protected to allow subclasses to add stuff
     * there (i.e. interactive instantiations)
     */
    protected JPanel instantiationsPanel;

    /**
     * The underlying proof center.
     */
    protected ProofCenter proofCenter;
    
    /**
     * Instantiates a new rule application component.
     * 
     * @param proofCenter
     *            the proof center to use
     */
    public RuleApplicationComponent(ProofCenter proofCenter) {
        this.env = proofCenter.getEnvironment();
        this.proofCenter = proofCenter;
        makeGUI();

        proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
    }

    /**
     * Prepare the gui.
     */
    private void makeGUI() {
        this.setLayout(new GridBagLayout());
        // slot (0,0) in the gridbaglayout is filled in the interactive rule subclass
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Rule"));
            ruleText = new JTextArea();
            ruleText.setEditable(false);
            ruleText.setFocusable(false);
            ruleText.setBackground(getBackground());
            ruleText.setFont(RULE_FONT);
            panel.add(ruleText, BorderLayout.CENTER);
            add(panel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        {
            instantiationsPanel = new JPanel();
            instantiationsPanel.setLayout(new VerticalLayout());
            instantiationsPanel.setBorder(BorderFactory.createTitledBorder("Instantiations"));
            add(instantiationsPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        {
            // Fill the remaining space with empty component
            add(Box.createGlue(),  new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
    }

    /**
     * Display a rule application.
     * 
     * called by {@link #setRuleApplication(RuleApplication)}.
     * 
     */
    private void displayRuleApp(RuleApplication app) {
        if(app == null) {
            ruleText.setText("");
            instantiationsPanel.removeAll();
        } else {
            setRuleText(app.getRule());
            setInstantiations(app);
        }
    }
    
    /**
     * Sets the instantiations.
     * 
     * Can be overridden.
     * 
     * @param app
     *            the new instantiations
     */
    protected void setInstantiations(RuleApplication app) {
        instantiationsPanel.removeAll();
        for (Map.Entry<String, Term> entry : app.getSchemaVariableMapping().entrySet()) {
            String schemaName = entry.getKey();
            Term t = entry.getValue();
            assert t != null;
            
            JLabel label = new JLabel(schemaName + " as " + t.getType());
            instantiationsPanel.add(label);
            
            JTextField textField = new JTextField();
            textField.setText(proofCenter.getPrettyPrinter().print(t).toString());
            textField.setEditable(false);
            instantiationsPanel.add(textField);
            instantiationsPanel.add(Box.createRigidArea(new Dimension(10,10)));
        }
        
    }

    /**
     * Using the pretty printer of the proof center, dump the rule to its text
     * area.
     * 
     * @param rule
     *            the rule to print
     */
    private void setRuleText(Rule rule) {
        PrettyPrint pp = proofCenter.getPrettyPrinter();
        ruleText.setText(pp.print(rule));
    }

    /**
     * Sets the rule application.
     * 
     * @param ruleApplication
     *            the new rule application
     */
    protected void setRuleApplication(RuleApplication ruleApplication) {
        this.ruleApplication = ruleApplication;
        displayRuleApp(ruleApplication);
    }

    /**
     * Gets the rule application.
     * 
     * @return the rule application
     */
    protected RuleApplication getRuleApplication() {
        return ruleApplication;
    }

    /**
     * Gets the proof center.
     * 
     * @return the proof center
     */
    protected ProofCenter getProofCenter() {
        return proofCenter;
    }

    /**
     * update the rule application view
     */
    private void update(ProofNode node) {
        setRuleApplication(node.getAppliedRuleApp());
    }

    /**
     * If selected ProofNode changed, get the corresponding rule application and
     * display it.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            update((ProofNode) evt.getNewValue());
        }
    }

    @Override
    public void handleNotification(NotificationEvent event) {
        update(getProofCenter().getCurrentProofNode());
    }

}

