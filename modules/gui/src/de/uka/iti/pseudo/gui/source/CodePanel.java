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
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.SequentHistory.Annotation;
import de.uka.iti.pseudo.term.CodeLocation;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

public abstract class CodePanel extends JPanel implements PropertyChangeListener, NotificationListener {

    private static final long serialVersionUID = -1207856898178542463L;

    private BreakpointPane sourceComponent;
    private Set<Object> knownResources;
    /*
     * Object is used for this box (when refitting this generic) since the
     * subclasses list different things in it.
     */
    private JComboBox<Object> selectionBox;
    protected final ProofCenter proofCenter;
    /**
     * This field holds the program term that has been identified
     * resonsible for the currently active term.
     * Line numbers can be extracted from that.
     */
    protected LiteralProgramTerm relevantProgramTerm;
    private final BreakpointManager breakpointManager;
    private Object displayedResource;

    private final Map<ProofNode, Collection<? extends CodeLocation<?>>> cache =
            new HashMap<ProofNode, Collection<? extends CodeLocation<?>>>();


    public CodePanel(ProofCenter proofCenter, boolean showLinenumbers,
            Color foregroundColor) throws IOException, StrategyException {
        this.proofCenter = proofCenter;
        this.breakpointManager = proofCenter.getBreakpointManager();
        proofCenter.addPropertyChangeListener(ProofCenter.CODE_PANE_SHOW_TRACE, this);
        proofCenter.addNotificationListener(TermComponent.TERM_COMPONENT_SELECTED_TAG, this);
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
            selectionBox = new JComboBox<Object>();
            // this set the preferred size to something smaller than the maximum display
            // size ... the panel can be resized smaller than that width ... (was a feature req)
            selectionBox.setPrototypeDisplayValue("minimalWidthDisplay");
            selectionBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectSource();
                }
            });
            add(selectionBox, BorderLayout.NORTH);
            knownResources = getAllResources(null);
            selectionBox.setModel(new DefaultComboBoxModel<Object>(knownResources.toArray()));
            selectSource();
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
        if (null == proofCenter.getCurrentProofNode()) {
            return;
        }
        addHighlights();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            // null can be sent if the selected node changed
            if (null == node) {
                return;
            }
            proofNodeSelected(node);
        } else if (ProofCenter.CODE_PANE_SHOW_TRACE.equals(evt.getPropertyName())) {
            BreakpointPane.showTrace = (Boolean) evt.getNewValue();

            getSourceComponent().removeHighlights();
            if (null == proofCenter.getCurrentProofNode()) {
                return;
            }
            addHighlights();
        }

    }

    @Override
    public void handleNotification(@NonNull NotificationEvent event) {
        assert event.isSignal(TermComponent.TERM_COMPONENT_SELECTED_TAG);

        try {
            TermComponent component = (TermComponent) event.getParameter(0);
            Annotation history = component.getHistory();
            LiteralProgramTerm reason = null;
            while(history != null && reason == null) {
                ProofNode proofNode = history.getCreatingProofNode();
                if(proofNode == null) {
                    break;
                }
                RuleApplication ruleApp = proofNode.getAppliedRuleApp();
                String rewrite = ruleApp.getRule().getProperty("rewrite");
                if(rewrite != null && rewrite.equals("symbex")) {
                    Term term = ruleApp.getSchemaVariableMapping().get("%a");
                    reason = (LiteralProgramTerm) term;
                } else {
                    history = history.getParentAnnotation();
                }
            }

            this.relevantProgramTerm = reason;
        } catch (Exception e) {
            e.printStackTrace();
        }

        getSourceComponent().removeHighlights();
        if (null == proofCenter.getCurrentProofNode()) {
            return;
        }
        addHighlights();
    }

    private void proofNodeSelected(ProofNode node) {

        Set<Object> newResources = getAllResources(node);
        if(!newResources.equals(knownResources)) {
            selectionBox.setModel(new DefaultComboBoxModel<Object>(newResources.toArray()));
            knownResources = newResources;
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

    abstract protected Set<Object> getAllResources(ProofNode node);

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
