package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.where.Interactive;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Triple;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

public class RuleApplicationComponent extends JPanel implements ProofNodeSelectionListener {
    
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

    public void ruleApplicationSelected(RuleApplication ruleApp) {
        // do nothing ...
        // the interactive subclass does however react to this
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
        ruleText.setText(rule.prettyPrint(env));
    }

    public void proofNodeSelected(ProofNode node) {
        RuleApplication ruleApp = node.getAppliedRuleApp();
        setRuleApplication(ruleApp);
    }

    protected void setRuleApplication(RuleApplication ruleApplication) {
        this.ruleApplication = ruleApplication;
        displayRuleApp(ruleApplication);
    }

    protected RuleApplication getRuleApplication() {
        return ruleApplication;
    }

    
}
