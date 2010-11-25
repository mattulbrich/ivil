/**
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * This component displays all open goals of a tree.
 * 
 * It is aware of changes of the selected tree node and reacts to changes on the
 * proof.
 * 
 * However, not every single change of the proof tree is reflected. Only upon
 * committing to changes ({@link ProofCenter#PROOFTREE_HAS_CHANGED}) the
 * corresponding notification event updates the list of open goals.
 * 
 * Handling selection is done by a listener in the main window.
 * 
 * @author mattias ulbrich
 */
public class GoalList extends JList implements PropertyChangeListener, NotificationListener {

    private static final long serialVersionUID = 4802864505685652999L;
    private Proof proof;
    private Model model;

    public GoalList(ProofCenter proofCenter) {
        this.proof = proofCenter.getProof();
        this.model = new Model();
        setModel(model);
        setCellRenderer(new Renderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, this);
        proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);

        // display initial goals
        model.update();
        setSelectedValue(proof.getRoot(), true);
    }

    @SuppressWarnings("serial")
    private static class Renderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ProofNode) {
                ProofNode proofNode = (ProofNode) value;
                return super.getListCellRendererComponent(list, proofNode
                        .getSummaryString(), index, isSelected, cellHasFocus);
            } else {
                return super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
            }
        }
    }

    private class Model implements ListModel {

        private Object[] openGoals = new Object[0];
        private int countGoals;

        /** Listeners. */
        protected EventListenerList listenerList = new EventListenerList();

        public void addListDataListener(ListDataListener l) {
            listenerList.add(ListDataListener.class, l);
        }

        public void removeListDataListener(ListDataListener l) {
            listenerList.remove(ListDataListener.class, l);
        }

        public Object getElementAt(int index) {
            return openGoals[index];
        }

        public int getSize() {
            return countGoals;
        }
        
        private void update() {
            // Make a copy of the open goals so that unfortunate scheduling
            // does no harm afterwards. openGoals is synchronised.
            openGoals = proof.getOpenGoals().toArray();
            countGoals = openGoals.length;

            ListDataEvent event = new ListDataEvent(GoalList.this,
                    ListDataEvent.CONTENTS_CHANGED, 0, getSize());
            for (ListDataListener listener : listenerList
                    .getListeners(ListDataListener.class)) {
                listener.contentsChanged(event);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            if (getLastVisibleIndex() != -1) {
                clearSelection();
                setSelectedValue(node, true);
            }
        }
    }
    
    @Override
    public void handleNotification(NotificationEvent evt) {
        
        assert EventQueue.isDispatchThread();
        
        if(evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            model.update();
        }
    }

    /* we are sure we only have proofnodes */
    public ProofNode getSelectedProofNode() {
        ProofNode selectedValue = (ProofNode) getSelectedValue();
        return selectedValue;
    }
}
