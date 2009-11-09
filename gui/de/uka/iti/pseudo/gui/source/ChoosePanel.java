package de.uka.iti.pseudo.gui.source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.ProofNodeSelectionListener;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

public abstract class ChoosePanel extends JPanel implements ProofNodeSelectionListener {

    private BreakpointPane sourceComponent;
    private int numberOfKnownPrograms = 0;
    private JComboBox selectionBox;
    private ProofCenter proofCenter;
    private BreakpointManager breakpointManager;
    private Object displayedResource;

    
    public ChoosePanel(ProofCenter proofCenter, boolean showLinenumbers, 
            Color foregroundColor) throws IOException {
        this.proofCenter = proofCenter;
        this.breakpointManager = proofCenter.getBreakpointManager();
        init(showLinenumbers, foregroundColor); 
    }
    
    private void init(boolean showLinenumbers, Color foregroundColor) throws IOException {
        setLayout(new BorderLayout());
        {
            sourceComponent = new BreakpointPane(breakpointManager, showLinenumbers);
            sourceComponent.setForeground(foregroundColor);
            add(sourceComponent, BorderLayout.CENTER);
        }
        {
            selectionBox = new JComboBox();
            selectionBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectSource();
                }
            });
            add(selectionBox, BorderLayout.NORTH);
            selectionBox.setModel(updatePrograms());
            selectSource();
            numberOfKnownPrograms = proofCenter.getEnvironment().getAllPrograms().size(); 
        }
    }
    
    private void selectSource() {
        displayedResource = selectionBox.getSelectedItem();
        sourceComponent.setText(makeContent(displayedResource));
        sourceComponent.setBreakPointResource(displayedResource);
    }
    
    public void proofNodeSelected(ProofNode node) {
        int now = proofCenter.getEnvironment().getAllPrograms().size();
        if(now != numberOfKnownPrograms) {
            selectionBox.setModel(updatePrograms());
            selectSource();
            numberOfKnownPrograms = proofCenter.getEnvironment().getAllPrograms().size();
        }
    }

    public void ruleApplicationSelected(RuleApplication ruleApplication) {
        // do nothing
    }

    abstract protected String makeContent(Object reference);

    abstract protected ComboBoxModel updatePrograms();

    protected ProofCenter getProofCenter() {
        return proofCenter;
    }

    public BreakpointPane getSourceComponent() {
        return sourceComponent;
    }

    public Object getDisplayedResource() {
        return displayedResource;
    }
}
