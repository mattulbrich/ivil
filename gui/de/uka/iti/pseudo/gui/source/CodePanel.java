/*
 * This file is part of PSEUDO
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

public abstract class CodePanel extends JPanel implements PropertyChangeListener {

    private BreakpointPane sourceComponent;
    private int numberOfKnownPrograms = 0;
    private JComboBox selectionBox;
    private ProofCenter proofCenter;
    private BreakpointManager breakpointManager;
    private Object displayedResource;
    private List<LiteralProgramTerm> foundProgramTerms = new ArrayList<LiteralProgramTerm>();
    
    private TermVisitor programFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(LiteralProgramTerm progTerm) throws TermException {
            foundProgramTerms.add(progTerm);
        }
    };
    
    public CodePanel(ProofCenter proofCenter, boolean showLinenumbers, 
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
    }
    
    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        if(ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            proofNodeSelected(node);
        }
        
    }
    
    private void proofNodeSelected(ProofNode node) {
        int now = proofCenter.getEnvironment().getAllPrograms().size();
        if(now != numberOfKnownPrograms) {
            selectionBox.setModel(getAllResources());
            numberOfKnownPrograms = proofCenter.getEnvironment().getAllPrograms().size();
        }
        
        recalcProgramTerms(node);
        
        Object resource = chooseResource();
        if(resource != null && resource != displayedResource) {
            selectionBox.setSelectedItem(resource);
            // this assertion ensures that the resource was in the list
            assert getDisplayedResource() == resource;
        }
        
        getSourceComponent().removeHighlights();
        addHighlights();
    }
    
    private void recalcProgramTerms(ProofNode node) {

        foundProgramTerms.clear();
        try {
            for (Term t : node.getSequent().getAntecedent()) {
                t.visit(programFindVisitor);
            }

            for (Term t : node.getSequent().getSuccedent()) {
                t.visit(programFindVisitor);
            }
        } catch (TermException e) {
            // never thrown
            throw new Error(e);
        }

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
    
    public List<LiteralProgramTerm> getFoundProgramTerms() {
        return foundProgramTerms;
    }
    
}
