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
import java.util.List;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

// TODO DOC

public class RuleApplicationComponent extends JPanel implements ProofNodeSelectionListener, ListSelectionListener, ActionListener, Observer {
    
    private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private static final String NEWLINE = "\n";

    private List<RuleApplication> interactiveApplications;
    
    private RuleApplication ruleApplication;

    private Environment env;

    private JList applicableList;

    private JTextArea ruleText;

    private JPanel instantiationsPanel;

    private DefaultListModel applicableListModel;

    private JPanel applicableListPanel;
    private ProofCenter proofCenter;

    public RuleApplicationComponent(ProofCenter proofCenter) {
        this.env = proofCenter.getEnvironment();
        this.proofCenter = proofCenter;
        proofCenter.getProof().addObserver(this);
        makeGUI();
    }

    private void makeGUI() {
        this.setLayout(new GridBagLayout());
        {
            applicableListPanel = new JPanel(new BorderLayout());
            applicableListPanel.setBorder(BorderFactory.createTitledBorder("Applicable rules"));
            applicableList = new JList();
            applicableListModel = new DefaultListModel();
            applicableList.setModel(applicableListModel);
            applicableList.setBorder(BorderFactory.createEtchedBorder());
            applicableList.addListSelectionListener(this);
            applicableListPanel.add(applicableList, BorderLayout.CENTER);
            add(applicableListPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            applicableListPanel.setVisible(false);
            
            // action listener for the poor
            applicableList.addMouseListener(new MouseAdapter() { 
                public void mouseClicked(MouseEvent e) {
                    if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                        actionPerformed(null);
                }
            });
        }
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

    public void setInteractiveApplications(List<RuleApplication> apps) {
        this.interactiveApplications = apps;
        this.ruleApplication = null;
        applicableListModel.removeAllElements();
        
        if(apps.isEmpty()) {
            applicableListModel.addElement("No applicable rules");
            applicableList.setEnabled(false);
        } else {
            for (RuleApplication ruleApplication : apps) {
                applicableListModel.addElement(ruleApplication);
            }
            applicableList.setEnabled(true);
        }
        instantiationsPanel.removeAll();
        ruleText.setText("");
        applicableListPanel.setVisible(true);
    }
    
    public void setAppliedRule(RuleApplication ruleApp) {
        this.ruleApplication = ruleApp;
        this.interactiveApplications = null;
        applicableListPanel.setVisible(false);
        displayRuleApp(ruleApp);
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
    
    private void setInstantiations(RuleApplication app) {
        instantiationsPanel.removeAll();
        for (String schemaName : app.getSchemaVariableNames()) {
            JLabel label = new JLabel(schemaName);
            instantiationsPanel.add(label);
            
            JTextField textField = new JTextField();
            Term t = app.getTermInstantiation(schemaName);
            if(t != null) {
                textField.setText(PrettyPrint.print(env, t).toString());
                textField.setEditable(false);
            } else {
                textField.addActionListener(this);
            }
            instantiationsPanel.add(textField);
            instantiationsPanel.add(Box.createRigidArea(new Dimension(10,10)));
        }
        instantiationsPanel.add(Box.createVerticalGlue());
    }

    private void setRuleText(Rule rule) {
        ruleText.setText(rule.prettyPrint(env));
    }

    public void proofNodeSelected(ProofNode node) {
        RuleApplication ruleApp = node.getAppliedRuleApp();
        if(ruleApp != null)
            setAppliedRule(ruleApp);
        // otherwise ? everthing to null?
    }

    public void valueChanged(ListSelectionEvent e) {
        // ignore those "adjusting" events
        if(e.getValueIsAdjusting())
            return;
        
        Object selected = applicableList.getSelectedValue();
        if (selected instanceof ImmutableRuleApplication) {
            RuleApplication ruleApp = (RuleApplication) selected;
            displayRuleApp(ruleApp);
        }
    }

    // chosen ruleappl
    @Override 
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
        Object selected = applicableList.getSelectedValue();
        if (selected instanceof ImmutableRuleApplication) {
            // TODO this works if no user instantiations
            try {
                proofCenter.apply((RuleApplication) selected);
            } catch (ProofException e1) {
                // TODO gescheite Fehlerausgabe
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        assert o == proofCenter.getProof();
        ProofNode proofNode = (ProofNode) arg;
        setAppliedRule(null);
    }

}
