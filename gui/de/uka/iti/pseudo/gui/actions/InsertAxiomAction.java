package de.uka.iti.pseudo.gui.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;

@SuppressWarnings("serial")
public class InsertAxiomAction extends BarAction implements InitialisingAction,
        PropertyChangeListener {

    private Rule axiomRule;

    public InsertAxiomAction() {
        super("Insert axiom", GUIUtil.makeIcon(SnapshotManager.class
                .getResource("img/book_next.png")));
        putValue(SHORT_DESCRIPTION, "lets you choose an axiom of the environment to insert into the sequent.");
    }

    public void initialised() {
        ProofCenter proofCenter = getProofCenter();
        if (proofCenter != null) {
            
            axiomRule = proofCenter.getEnvironment().getRule("axiom");
            
            if(axiomRule == null) {
                setEnabled(false);
            } else {
                proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
            }
        }
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean) evt.getNewValue());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        assert axiomRule != null : "this should only come up if the rule is defined";
        
        ProofCenter pc = getProofCenter();
        Environment env = pc.getEnvironment();
        AxiomChooserDialog dlg = new AxiomChooserDialog(getParentFrame(),
                env, pc.getPrettyPrinter());
        
        Log.log(Log.TRACE, "before set visible");
        dlg.setVisible(true);
        String axiomName = dlg.getAxiomName();
        
        Log.log("Selected axiom: " + axiomName);
        
        if(axiomName != null) {
            Axiom axiom = env.getAxiom(axiomName);
            Proof proof = pc.getProof();
            
            assert axiom != null : "the axiom must be found in the environment";
            
            MutableRuleApplication ruleApp = new MutableRuleApplication();
            ruleApp.getProperties().put("axiomName", axiomName);
            ruleApp.setRule(axiomRule);
            ruleApp.getSchemaVariableMapping().put("%b", axiom.getTerm());
            
            // TODO put this inside the lock! (or threaded)
            ProofNode node = pc.getCurrentProofNode();
            
            ruleApp.setProofNode(node);
            try {
                proof.apply(ruleApp, env);
            } catch (ProofException ex) {
                ex.printStackTrace();
            }
            pc.fireProoftreeChangedNotification(true);
            
        }
    }


}

@SuppressWarnings("serial")
class AxiomChooserDialog extends JDialog {
    
    private String axiomName = null;

    private JList axiomList;
    
    /**
     * The font to use for printing rules
     */
    private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);

    public AxiomChooserDialog(Frame parentFrame, final Environment environment, final PrettyPrint prettyPrint) {
        super(parentFrame, true);
        setTitle("Choose axiom to insert");
        Container cp = getContentPane();
        {
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
            split.setDividerLocation(230);
            {
                axiomList = new JList(collectAxiomNames(environment));
                split.setTopComponent(new JScrollPane(axiomList));
            }
            {
                final JTextArea axiomFormula = new JTextArea();
                axiomFormula.setFont(RULE_FONT);
                axiomFormula.setLineWrap(true);
                axiomList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        Log.enter(e);
                        if(e.getValueIsAdjusting())
                            return;
                        if(axiomList.isSelectionEmpty()) {
                            axiomFormula.setText("");
                        } else {
                            String name = (String)axiomList.getSelectedValue();
                            Axiom ax = environment.getAxiom(name);
                            axiomFormula.setText(prettyPrint.print(ax.getTerm()).toString());
                        }
                    }
                });
                split.setBottomComponent(new JScrollPane(axiomFormula));
            }
            cp.add(split, BorderLayout.CENTER);
        }
        {
            JPanel buttons = new JPanel();
            {
                JButton cancel = new JButton("Cancel");
                cancel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        axiomName = null;
                        dispose();
                    }
                });
                buttons.add(cancel);
            }
            {
                final JButton ok = new JButton("OK");
                ok.setEnabled(!axiomList.isSelectionEmpty());
                axiomList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        Log.enter(e);
                        ok.setEnabled(!axiomList.isSelectionEmpty());
                    }
                });
                ok.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        axiomName = (String)axiomList.getSelectedValue();
                        dispose();
                    }
                });
                buttons.add(ok);
                getRootPane().setDefaultButton(ok);
            }
            cp.add(buttons, BorderLayout.SOUTH);
        }
        setSize(300,400);
        setLocationRelativeTo(parentFrame);
    }

    private Vector<String> collectAxiomNames(Environment environment) {
        Log.enter(System.currentTimeMillis());
        Vector<String> ret = new Vector<String>();
        for (Axiom axiom : environment.getAllAxioms()) {
            ret.add(axiom.getName());
        }
        Log.log(Log.TRACE, System.currentTimeMillis());
        Log.leave();
        return ret;
    }
    
    /**
     * @return the axiomName
     */
    public String getAxiomName() {
        return axiomName;
    }

    
}
