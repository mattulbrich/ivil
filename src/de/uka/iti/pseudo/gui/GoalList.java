package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;

//TODO DOC

public class GoalList extends JList implements ProofNodeSelectionListener {

    private static final long serialVersionUID = 4802864505685652999L;
    private Proof proof;
    private Environment env;
    
    public GoalList(Proof proof, Environment env) {
        this.proof = proof;
        this.env = env;
        Model model = new Model();
        setModel(model);
        proof.addObserver(model);
        setCellRenderer(new Renderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private class Renderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            if (value instanceof ProofNode) {
                ProofNode proofNode = (ProofNode) value;
                return super.getListCellRendererComponent(list, proofNode.getSummaryString(), index, isSelected, cellHasFocus);
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

    private class Model implements ListModel, Observer {

        /** Listeners. */
        protected EventListenerList listenerList = new EventListenerList();
        
        public void addListDataListener(ListDataListener l) {
            listenerList.add(ListDataListener.class, l);
        }
        public void removeListDataListener(ListDataListener l) {
            listenerList.remove(ListDataListener.class, l);
        }
        
        public Object getElementAt(int index) {
            return proof.getGoal(index);
        }

        public int getSize() {
            return proof.getOpenGoals().size();
        }

        public void update(Observable o, Object arg) {
            assert o == proof;
            ListDataEvent event = new ListDataEvent(GoalList.this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
            for (ListDataListener listener : listenerList.getListeners(ListDataListener.class)) {
                listener.contentsChanged(event);
            }
        }
        
    }

    public void proofNodeSelected(ProofNode node) {
        clearSelection();
        setSelectedValue(node, true);
    }

    /* we are sure we only have proofnodes */
    public ProofNode getSelectedProofNode() {
        ProofNode selectedValue = (ProofNode) getSelectedValue();
        return selectedValue;
    }

}
