package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import de.uka.iti.pseudo.gui.ProofComponent;
import de.uka.iti.pseudo.util.GUIUtil;

@SuppressWarnings("serial") 
public class ExpandProofTreeBelowAction extends BarAction {
    
    private static Icon plusIcon = 
        GUIUtil.makeIcon(AutoProofAction.class.getResource("img/bullet_toggle_plus.png"));
    
    public ExpandProofTreeBelowAction() {
        super("Expand the proof tree below", plusIcon);
    }

    @Override public void actionPerformed(ActionEvent e) {
        ProofComponent proofComponent = getProofCenter().getMainWindow().getProofComponent();
        int[] selections = proofComponent.getSelectionRows();
        if(selections != null && selections.length > 0) {
            proofComponent.expandRow(selections[0]);
        }
    }
    
}
