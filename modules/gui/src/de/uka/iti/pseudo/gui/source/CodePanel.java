/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nonnull.Nullable;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.CodeLocation;

public abstract class CodePanel extends JPanel implements PropertyChangeListener {

    private static final long serialVersionUID = -1207856898178542463L;

    private BreakpointPane sourceComponent;
    private int numberOfKnownPrograms = 0;
    private JComboBox selectionBox;
    protected final ProofCenter proofCenter;
    private BreakpointManager breakpointManager;
    private Object displayedResource;

    private Map<ProofNode, Collection<? extends CodeLocation<?>>> cache =
            new HashMap<ProofNode, Collection<? extends CodeLocation<?>>>();
    
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
    
    protected void selectSource() {
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
        if (resource != null && !resource.equals(displayedResource)) {
            selectionBox.setSelectedItem(resource);

            // this assertion ensures that the resource was in the list
            assert resource.equals(displayedResource) : resource + " vs. " + getDisplayedResource();
        }
        
        getSourceComponent().removeHighlights();
        addHighlights();
    }
    
    protected Collection<? extends CodeLocation<?>> getCodeLocations(ProofNode node) {
        Collection<? extends CodeLocation<?>> result =
                cache.get(node);
        if(result == null) {
            result = calculateCodeLocationsOfNode(node);
            cache .put(node, result);
        }
        return result;
    }
    
    protected @Nullable Object chooseResource() {
        Collection<? extends CodeLocation<?>> locations = 
                getCodeLocations(proofCenter.getCurrentProofNode());
        
        if (locations.size() == 0) {
            return null;
        }
        
        return locations.iterator().next().getProgram();
    }

    abstract protected String makeContent(Object reference);

    abstract protected ComboBoxModel getAllResources();
    
    abstract protected void addHighlights();
    
    abstract protected Collection<? extends CodeLocation<?>> calculateCodeLocationsOfNode(ProofNode node);

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
