package de.uka.iti.pseudo.gui.actions.sequent;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.where.KnownFormula;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.Pair;

/**
 * This action shows a menu which allows to reintroduce terms that were
 * explicitly hidden.
 * 
 * @author timm.felden@felden.com
 */
public class ReintroduceHiddenTermAction extends BarAction implements InitialisingAction, PropertyChangeListener,
        NotificationListener {

    private static final long serialVersionUID = 5513782367544705985L;

    private Rule leftRule, rightRule;

    public ReintroduceHiddenTermAction() throws ProofException {
        super("Reintroduce hidden term");
        putValue(SHORT_DESCRIPTION, "Allows you to reintroduce a term that was hidden earlier.");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
    }

    public void initialised() {
        ProofCenter proofCenter = getProofCenter();
        if (proofCenter != null) {

            leftRule = getProofCenter().getEnvironment().getRule("unhide_left");
            rightRule = getProofCenter().getEnvironment().getRule("unhide_right");

            if (null == leftRule || null == rightRule) {
                setEnabled(false);
                putValue(SHORT_DESCRIPTION, "Missing unhide rule, load \"$plugins.p\".");
            } else {
                proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
                proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
            }
        }

    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean) evt.getNewValue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Vector<DataRecord> terms = searchForHiddenTerms(getProofCenter().getCurrentProofNode());

        ProofCenter pc = getProofCenter();
        Environment env = pc.getEnvironment();
        Proof proof = pc.getProof();

        TermChooserDialog dlg = new TermChooserDialog(getParentFrame(), proof, pc.getPrettyPrinter(), terms);

        Log.log(Log.TRACE, "before set visible");
        dlg.setVisible(true);
        DataRecord selection = dlg.getSelection();


        if (selection != null) {
            Log.log("Selected term: " + selection.term);


            MutableRuleApplication ruleApp = new MutableRuleApplication();
            ruleApp.getProperties().put(KnownFormula.KNOWN_FORMULA_PROPERY,
                    selection.location.fst() + ":" + selection.location.snd());
            ruleApp.setRule(selection.location.snd().isAntecedent() ? leftRule : rightRule);
            ruleApp.getSchemaVariableMapping().put("%b", selection.term);

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

    @Override
    public void handleNotification(NotificationEvent evt) {
        if (evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            setEnabled(getProofCenter().getProof().hasOpenGoals());
        }
    }

    /**
     * Unhide actions don't happen frequently, so no special mechanism is
     * provided to store hidden terms. To get all hidden terms, we have to
     * ascend from the currentProofNode to the root and collect all terms that
     * have been hidden.
     */
    private Vector<DataRecord> searchForHiddenTerms(ProofNode currentProofNode) {

        Vector<DataRecord> rval = new Vector<DataRecord>();

        for (ProofNode node = currentProofNode.getParent(), child = currentProofNode; null != node; child = node, node = node
                .getParent()) {

            final RuleApplication ra = node.getAppliedRuleApp();

            // the search ends, if this node was not created by a copy action
            for (int i = 0; i < node.getChildren().size(); i++) {
                if (child != node.getChildren().get(i))
                    continue;

                if (GoalAction.Kind.COPY != ra.getRule().getGoalActions().get(i).getKind())
                    return rval;

                break;
            }

            final String hidingTag = ra.getRule().getProperty("hiding");
            // skip rules that did not hide anything
            if (null == hidingTag)
                continue;

            try {
                // hiding can be a list of "find" and schema variable names
                final String[] hiddenLocations = hidingTag.split(",");

                for (String location : hiddenLocations) {
                    TermSelector target;
                    if ("find".equals(location)) {
                        // the find clause was hidden
                        target = ra.getFindSelector();

                    } else if (location.startsWith("a")) {
                        // an assumption was hidden
                        int index = Integer.parseInt(location.substring(1));
                        target = ra.getAssumeSelectors().get(index);

                    } else
                        throw new IllegalArgumentException("The location '" + location + "' is illegal.");

                    if (!target.isToplevel())
                        throw new IllegalArgumentException("The target from " + location + " is not a toplevel term.");

                    Term term = target.selectSubterm(node.getSequent());
                    rval.add(new DataRecord(new Pair<Integer, TermSelector>(node.getNumber(), target), term, node
                            .getNumber()
                            + ":" + target.toString() + " "
                            + term));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.log(Log.ERROR, "The Rule " + ra.getRule().getName()
                        + " is not properly formulated respective to the hiding tag.");
            }
        }

        return rval;
    }

    /**
     * simple structure which stores data for hidden term entries.
     */
    static final class DataRecord {
        final Pair<Integer, TermSelector> location;
        final Term term;
        /**
         * a representation of the term truncated to 40 characters
         */
        final String shortRepresentation;

        public DataRecord(final Pair<Integer, TermSelector> location, final Term term, String textualRepresentation) {
            this.location = location;
            this.term = term;

            if (textualRepresentation.length() > 40)
                shortRepresentation = textualRepresentation.subSequence(0, 36) + "...";
            else
                this.shortRepresentation = textualRepresentation;
        }

        @Override
        public String toString() {
            return shortRepresentation;
        }
    }

    static class TermChooserDialog extends JDialog {

        private static final long serialVersionUID = 6476000033251755919L;

        private DataRecord selection = null;

        private JList termList;

        /**
         * The font to use for printing rules
         */
        private static final Font RULE_FONT = new Font("Monospaced", Font.PLAIN, 12);

        public TermChooserDialog(Frame parentFrame, final Proof proof, final PrettyPrint prettyPrint,
                final Vector<DataRecord> locations) {
            super(parentFrame, true);
            setTitle("Choose axiom to insert");
            Container cp = getContentPane();
            {
                JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                split.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
                split.setDividerLocation(230);
                {
                    termList = new JList(locations);
                    split.setTopComponent(new JScrollPane(termList));
                }
                {
                    final JTextArea termFormula = new JTextArea();
                    termFormula.setFont(RULE_FONT);
                    termFormula.setLineWrap(true);
                    termList.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            Log.enter(e);
                            if (e.getValueIsAdjusting())
                                return;
                            if (termList.isSelectionEmpty()) {
                                termFormula.setText("");
                            } else {
                                DataRecord data = (DataRecord) termList.getSelectedValue();
                                termFormula.setText(prettyPrint.print(data.term).toString());
                            }
                        }
                    });
                    split.setBottomComponent(new JScrollPane(termFormula));
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
                            selection = null;
                            dispose();
                        }
                    });
                    buttons.add(cancel);
                }
                {
                    final JButton ok = new JButton("OK");
                    ok.setEnabled(!termList.isSelectionEmpty());
                    termList.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            Log.enter(e);
                            ok.setEnabled(!termList.isSelectionEmpty());
                        }
                    });
                    ok.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            selection = (DataRecord) termList.getSelectedValue();
                            dispose();
                        }
                    });
                    buttons.add(ok);
                    getRootPane().setDefaultButton(ok);
                }
                cp.add(buttons, BorderLayout.SOUTH);
            }
            setSize(300, 400);
            setLocationRelativeTo(parentFrame);
        }

        /**
         * @return the selected term
         */
        public DataRecord getSelection() {
            return selection;
        }

    }
}
