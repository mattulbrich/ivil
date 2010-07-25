/**
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich and Timm Felden
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;

//TODO DOC

public class GoalList extends JList implements PropertyChangeListener {

    private static final long serialVersionUID = 4802864505685652999L;
    private Proof proof;

    public GoalList(Proof proof, Environment env) {
        this.proof = proof;
        Model model = new Model();
        setModel(model);
        proof.addObserver(model);
        setCellRenderer(new Renderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // display initial goals
        model.update(proof, null);
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

    private class Model implements ListModel, Observer {

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

        public void update(final Observable o, Object arg) {
            assert o == proof;

            // Make a copy of the open goals so that unfortunate scheduling
            // does no harm afterwards
            openGoals = proof.getOpenGoals().toArray();
            countGoals = openGoals.length;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ListDataEvent event = new ListDataEvent(GoalList.this,
                            ListDataEvent.CONTENTS_CHANGED, 0, getSize());
                    for (ListDataListener listener : listenerList
                            .getListeners(ListDataListener.class)) {
                        listener.contentsChanged(event);
                    }
                }
            });
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            ProofNode node = (ProofNode) evt.getNewValue();
            if (getSelectedValue() != node) {
                clearSelection();
                setSelectedValue(node, true);
            }
        }
    }

    /* we are sure we only have proofnodes */
    public ProofNode getSelectedProofNode() {
        ProofNode selectedValue = (ProofNode) getSelectedValue();
        return selectedValue;
    }
}
