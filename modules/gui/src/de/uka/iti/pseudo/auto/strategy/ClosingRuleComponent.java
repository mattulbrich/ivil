package de.uka.iti.pseudo.auto.strategy;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uka.iti.pseudo.gui.parameters.ParameterSheet;
import de.uka.iti.pseudo.rule.Rule;

@SuppressWarnings("serial")
public class ClosingRuleComponent extends JPanel
    implements PropertyChangeListener, ListSelectionListener {

    private SMTStrategy smtStrategy;
    private JList list;

    public ClosingRuleComponent() {
        super(new BorderLayout());
        addPropertyChangeListener(ParameterSheet.PARAM_SETTER, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        smtStrategy = (SMTStrategy) getClientProperty(ParameterSheet.PARAM_OBJECT);
        init();
    }

    private void init() {
        {
            Object shortDesc = getClientProperty(ParameterSheet.PARAM_SHORTDESC);
            if(shortDesc != null) {
                Border b = new TitledBorder(shortDesc.toString());
                setBorder(b);
            }
        }
        {
            Object[] rules = smtStrategy.getClosingRuleCollection().toArray();
            list = new JList(rules);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.addListSelectionListener(this);
            list.setSelectedValue(smtStrategy.getClosingRule(), true);
            this.add(list, BorderLayout.CENTER);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(smtStrategy != null) {
            smtStrategy.setClosingRule((Rule)list.getSelectedValue());
        }
    }

}
