package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;

public class RuleApplicationComponent extends JPanel implements ProofNodeSelectionListener {
    
    private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);

    private List<RuleApplication> interactiveApplications;
    
    private RuleApplication ruleApplication;

    private Environment env;

    private JList applicableList;
    

    private JTextArea ruleText;

    private JPanel instantiationsPanel;

    private DefaultListModel applicableListModel;

    public RuleApplicationComponent(Environment env) {
        this.env = env;
        makeGUI();
    }

    private void makeGUI() {
        this.setLayout(new GridBagLayout());
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Applicable rules"));
            applicableList = new JList();
            applicableListModel = new DefaultListModel();
            applicableList.setModel(applicableListModel);
            panel.add(applicableList, BorderLayout.CENTER);
            add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.7,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Rule"));
            ruleText = new JTextArea();
            ruleText.setEditable(false);
            ruleText.setEnabled(false);
            ruleText.setFont(RULE_FONT);
            panel.add(ruleText, BorderLayout.CENTER);
            add(panel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        {
            instantiationsPanel = new JPanel();
            instantiationsPanel.setLayout(new BoxLayout(instantiationsPanel, BoxLayout.Y_AXIS));
            instantiationsPanel.setBorder(BorderFactory.createTitledBorder("Instantiations"));
            add(instantiationsPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.7,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        
    }

    public void setInteractiveApplications(List<RuleApplication> apps) {
        this.interactiveApplications = apps;
        this.ruleApplication = null;
        applicableListModel.removeAllElements();
        for (RuleApplication ruleApplication : apps) {
            applicableListModel.addElement(ruleApplication);
        }
        applicableList.setEnabled(true);
    }
    
    public void setAppliedRule(RuleApplication ruleApp) {
        this.ruleApplication = ruleApp;
        this.interactiveApplications = null;
        applicableListModel.removeAllElements();
        applicableList.setEnabled(false);
        setRuleText(ruleApp.getRule());
    }
    
    private void setRuleText(Rule rule) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("rule ").append(rule.getName()).append("<br>");
        sb.append("  find ").append(rule.getFindClause()).append("<br>");
        for (LocatedTerm ass : rule.getAssumptions()) {
            sb.append("  assume ").append(ass).append("<br>");
        }
        for (WhereClause where : rule.getWhereClauses()) {
            sb.append("  where ").append(where).append("<br>");
        }
        for (GoalAction action : rule.getGoalActions()) {
            switch(action.getKind()) {
            case CLOSE: sb.append("  closegoal"); break;
            case COPY: sb.append("  samegoal"); break;
            case NEW: sb.append("  newgoal"); break;
            }
            sb.append("<br>");
            Term rep = action.getReplaceWith();
            if(rep != null)
                sb.append("    replace ").append(rep).append("<br>");
            for (Term t : action.getAddAntecedent()) {
                sb.append("    add ").append(t).append(" |-<br>");
            }
            for (Term t : action.getAddSuccedent()) {
                sb.append("    add |-").append(t).append("<br>");
            }
        }
        ruleText.setText(sb.toString());
    }

    public void proofNodeSelected(ProofNode node) {
        RuleApplication ruleApp = node.getAppliedRuleApp();
        if(ruleApp != null)
            setAppliedRule(ruleApp);
        // otherwise ? everthing to null?
    }

   

}
