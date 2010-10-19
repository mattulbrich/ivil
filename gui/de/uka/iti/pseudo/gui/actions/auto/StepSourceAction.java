package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.actions.io.LoadProblemAction;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * if the currently selected proof node is an open goal and has a unique line
 * number, the currently active strategy will be applied until all children are
 * either closed or have another unique line number.
 */
public class StepSourceAction extends StepCodeAction {

    private static final long serialVersionUID = 5225203245082459198L;

    public StepSourceAction() {
        super("Step Source", GUIUtil.makeIcon(LoadProblemAction.class
.getResource("../img/control_fastforward.png")));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        putValue(SHORT_DESCRIPTION,
                "symbolically execute a single source code instruction");

    }

    @Override
    protected CodeLocation getCodeLocation(ProofNode node) {

        final List<LiteralProgramTerm> progTerms = new LinkedList<LiteralProgramTerm>();

        TermVisitor programFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
            public void visit(LiteralProgramTerm progTerm) throws TermException {
                progTerms.add(progTerm);
            }
        };
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

        if (progTerms.isEmpty()) {
            return null;
        }

        CodeLocation rval = new CodeLocation();
        rval.isUnique = true;
        rval.line = progTerms.get(0).getStatement().getSourceLineNumber();
        rval.program = progTerms.get(0).getProgram().getSourceFile();

        if (null == rval.program) // no source file found
            return null;

        // check other program terms for equality
        for (int i = 1; i < progTerms.size(); i++){
            if (progTerms.get(i).getStatement().getSourceLineNumber() != rval.line ||
                    !rval.program.equals(progTerms.get(i).getProgram().getSourceFile())){
                rval.isUnique = false;
                return rval;
            }
        }

        return rval;
    }

}