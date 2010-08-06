/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.util.Triple;

// TODO DOC

@SuppressWarnings("serial") 
public class RuleApplicationComponent extends JPanel implements PropertyChangeListener {
    
    private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);

    private RuleApplication ruleApplication;

    protected Environment env;

    private JTextArea ruleText;

    // protected for interactive fields added by a subclass
    protected JPanel instantiationsPanel;

    protected ProofCenter proofCenter;
    
    protected List<Triple<String, Type, ? extends JTextComponent>> interactionList = 
        new ArrayList<Triple<String, Type, ? extends JTextComponent>>();

    public RuleApplicationComponent(ProofCenter proofCenter) {
        this.env = proofCenter.getEnvironment();
        this.proofCenter = proofCenter;
        makeGUI();
    }

    private void makeGUI() {
        this.setLayout(new GridBagLayout());
        // slot (0,0) in the gridbaglayout is filled in the interactive rule subclass
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Rule"));
            ruleText = new JTextArea();
            ruleText.setEditable(false);
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

    private void displayRuleApp(RuleApplication app) {
        if(app == null) {
            ruleText.setText("");
            instantiationsPanel.removeAll();
        } else {
            setRuleText(app.getRule());
            setInstantiations(app);
        }
    }
    
    protected void setInstantiations(RuleApplication app) {
        instantiationsPanel.removeAll();
        interactionList.clear();
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

    private void setRuleText(Rule rule) {
        PrettyPrint pp = proofCenter.getPrettyPrinter();
        ruleText.setText(pp.print(rule));
    }
    
    /*
     * This class only reacts to "select proof node" events. The interactive
     * subclass also reacts to rule application setting.
     */  
    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        if(ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            // null can be sent if the selected node changed
            if (null == node)
                return;

            RuleApplication ruleApp = node.getAppliedRuleApp();
            setRuleApplication(ruleApp);
        }
    }

    protected void setRuleApplication(RuleApplication ruleApplication) {
        this.ruleApplication = ruleApplication;
        displayRuleApp(ruleApplication);
    }

    protected RuleApplication getRuleApplication() {
        return ruleApplication;
    }

    protected ProofCenter getProofCenter() {
        return null;
    }

    
}
