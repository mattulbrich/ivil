package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.event.KeyEvent;
import java.net.URL;
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
public class StepSourceAction extends StepCodeAction {

    private static final long serialVersionUID = 5225203245082459198L;

    public StepSourceAction() {
        super("Step Source");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        putValue(SHORT_DESCRIPTION,
                "symbolically execute a single source code instruction");

    }

    @Override
    protected CodeLocation<?> getCodeLocation(ProofNode node) {
        Set<CodeLocation<Program>> codeLocs = node.getCodeLocations();
        Set<CodeLocation<URL>> sourceLocs = CodeLocation.findSourceCodeLocations(codeLocs);
        if (sourceLocs.size() == 1)
            return sourceLocs.iterator().next();
        return null;
    }

}