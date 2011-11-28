package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.CodeLocation;

/**
 * if the currently selected proof node is an open goal and has a unique line
 * number, the currently active strategy will be applied until all children are
 * either closed or have another unique line number.
 */
public class StepInstructionAction extends StepCodeAction {
    
    private static final long serialVersionUID = -6585879229802844874L;

    public StepInstructionAction() {
        super("Step Instruction");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        putValue(SHORT_DESCRIPTION, "symbolically execute a single intermediate code instruction");

    }

    @Override
    protected CodeLocation<?> getCodeLocation(ProofNode node) {
        Set<CodeLocation<Program>> nativeCodeLocations = node.getCodeLocations();
        if (nativeCodeLocations.size() == 1)
            return nativeCodeLocations.iterator().next();
        return null;
    }

}