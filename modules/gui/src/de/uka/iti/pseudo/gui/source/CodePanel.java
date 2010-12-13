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
package de.uka.iti.pseudo.gui.source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.ProofNode;

public abstract class CodePanel extends JPanel implements PropertyChangeListener {

    private static final long serialVersionUID = -1207856898178542463L;

    private BreakpointPane sourceComponent;
    private int numberOfKnownPrograms = 0;
    private JComboBox selectionBox;
    protected final ProofCenter proofCenter;
    private BreakpointManager breakpointManager;
    private Object displayedResource;
    
    public CodePanel(ProofCenter proofCenter, boolean showLinenumbers, 
            Color foregroundColor) throws IOException, StrategyException {
        this.proofCenter = proofCenter;
        this.breakpointManager = proofCenter.getBreakpointManager();
        proofCenter.addPropertyChangeListener(ProofCenter.CODE_PANE_SHOW_TRACE, this);
        init(showLinenumbers, foregroundColor); 
    }
    
    private void init(boolean showLinenumbers, Color foregroundColor) throws IOException {
        setLayout(new BorderLayout());
        {
            sourceComponent = new BreakpointPane(breakpointManager, showLinenumbers);
            sourceComponent.setForeground(foregroundColor);
            JScrollPane scroll = new JScrollPane(sourceComponent);
            add(scroll, BorderLayout.CENTER);
        }
        {
            selectionBox = new JComboBox();
            selectionBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectSource();
                }
            });
            add(selectionBox, BorderLayout.NORTH);
            selectionBox.setModel(getAllResources());
            selectSource();
            numberOfKnownPrograms = proofCenter.getEnvironment().getAllPrograms().size(); 
        }
    }
    
    private void selectSource() {
        displayedResource = selectionBox.getSelectedItem();
        sourceComponent.setText(makeContent(displayedResource));
        // manually scroll to top
        sourceComponent.setLocation(0, 0);
        sourceComponent.setBreakPointResource(displayedResource);

        // create highlights for selected source
        sourceComponent.removeHighlights();
        if (null == proofCenter.getCurrentProofNode())
            return;
        addHighlights();
    }
    
    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        if(ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            // null can be sent if the selected node changed
            if (null == node)
                return;
            proofNodeSelected(node);
        } else if (ProofCenter.CODE_PANE_SHOW_TRACE.equals(evt.getPropertyName())) {
            BreakpointPane.showTrace = (Boolean) evt.getNewValue();

            getSourceComponent().removeHighlights();
            if (null == proofCenter.getCurrentProofNode())
                return;
            addHighlights();
        }
        
    }
    
    private void proofNodeSelected(ProofNode node) {
        int now = proofCenter.getEnvironment().getAllPrograms().size();
        if(now != numberOfKnownPrograms) {
            selectionBox.setModel(getAllResources());
            numberOfKnownPrograms = proofCenter.getEnvironment().getAllPrograms().size();
        }
        
        Object resource = chooseResource();
        if(resource != null && !resource.equals(displayedResource)) {
            selectionBox.setSelectedItem(resource);
            // this assertion ensures that the resource was in the list
            // FIXME FIXME FIXME why is that to be removed now?!?!?!
           // assert !resource.equals(displayedResource) : resource + " vs. " + getDisplayedResource();
        }
        
        getSourceComponent().removeHighlights();
        addHighlights();
    }

    abstract protected String makeContent(Object reference);

    abstract protected ComboBoxModel getAllResources();
    
    abstract protected Object chooseResource();
    
    abstract protected void addHighlights();

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
