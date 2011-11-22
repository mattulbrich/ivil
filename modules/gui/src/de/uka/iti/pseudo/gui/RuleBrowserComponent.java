package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

@SuppressWarnings("serial")
public class RuleBrowserComponent extends JPanel {

    private final ProofCenter proofCenter;
    
    /**
     * The font to use for printing rules
     */
    private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private final String[] MODES = { "By name", "By classification", "By verbosity" };
    private JTextArea ruleText;

    private JComboBox comboBox;

    private JComponent centerComponent;
    public RuleBrowserComponent(ProofCenter proofCenter) {
        super(new BorderLayout());
        this.proofCenter = proofCenter;
        init();
    }

    private void init() {
        {
            comboBox = new JComboBox(MODES);
            comboBox.setSelectedIndex(0);
            comboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createComponentFromCombobox();
                }
            });
            add(comboBox, BorderLayout.NORTH);
        }
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Rule:"));
            ruleText = new JTextArea();
            ruleText.setEditable(false);
            ruleText.setFocusable(false);
            ruleText.setBackground(getBackground());
            ruleText.setFont(RULE_FONT);
            panel.add(ruleText);
            add(panel, BorderLayout.SOUTH);
        }
        
        createComponentFromCombobox();
    }

    private void createComponentFromCombobox() {
        JComponent result;
        switch(comboBox.getSelectedIndex()) {
        case 0: // by name
            result = createByNameComponent();
            break;
        case 1:
            result = new JLabel("to be done ... 1");
            break;
        case 2:
            result = new JLabel("to be done ... 2");
            break;
        default:
            throw new Error("unhandled selection case: "
                    + comboBox.getSelectedIndex());
        }

        // replace last component
        if(centerComponent != null) {
            remove(centerComponent);
        }
        centerComponent = new JScrollPane(result);
        add(centerComponent, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent createByNameComponent() {
        final List<Rule> rules = proofCenter.getEnvironment().getAllRules();
        Collections.sort(rules, new Comparator<Rule>() {
            public int compare(Rule r1, Rule r2) {
                return r1.getName().compareToIgnoreCase(r2.getName());
            }
        });
        
        
        String[] data = new String[rules.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = rules.get(i).getName();
        }
        
        final JList result = new JList(data);
        // for the looks: otherwise the component is far too wide!
        Dimension preferredSize = result.getPreferredSize();
        preferredSize.width = result.getMinimumSize().width;
        result.setPreferredSize(preferredSize);
        
        result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        result.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting())
                    return;
                
                Log.enter(e);
                Rule rule = rules.get(result.getSelectedIndex());
                Log.log(Log.VERBOSE, "Rule to show: " + rule);
                setShownRule(rule);
            }
        });
        return result;
    }

    private void setShownRule(Rule rule) {
        PrettyPrint pp = proofCenter.getPrettyPrinter();
        ruleText.setText(pp.print(rule));
    }

}
