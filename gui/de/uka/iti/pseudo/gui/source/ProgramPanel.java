package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.PrettyPrint;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.settings.Settings;

public class ProgramPanel extends ChoosePanel {

    private static final Color PROGRAM_COLOR = Settings.getInstance()
            .getColor("pseudo.program.boogiecolor");

    private PrettyPrint prettyPrinter;
    private BreakpointManager breakpointManager;

    public ProgramPanel(ProofCenter proofCenter) throws IOException {
        super(proofCenter, false, PROGRAM_COLOR);
    }

    protected ComboBoxModel updatePrograms() {
        Collection<Program> programs = getProofCenter().getEnvironment()
                .getAllPrograms();

        return new DefaultComboBoxModel(programs.toArray());
    }

    protected String makeContent(Object object) {
        
        if(prettyPrinter == null)
            this.prettyPrinter = getProofCenter().getPrettyPrinter();
        
        Program p = (Program) object;
        StringBuilder sb = new StringBuilder();
        List<Statement> statements = p.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            sb.append(String.format("%3d: %s\n", i, prettyPrinter.print(
                    statements.get(i)).toString()));
        }
        return sb.toString();
    }
    
    @Override 
    public void proofNodeSelected(ProofNode node) {
        super.proofNodeSelected(node);
        
        getSourceComponent().removeHighlights();
        
        try {
            for (Term t : node.getSequent().getAntecedent()) {
                t.visit(selectionFindVisitor);
            }
            
            for (Term t : node.getSequent().getSuccedent()) {
                t.visit(selectionFindVisitor);
            }
        } catch (TermException e) {
            // never thrown
            throw new Error(e);
        }

    }
    
    private TermVisitor selectionFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(LiteralProgramTerm progTerm)
                throws TermException {
            if(progTerm.getProgram() == getDisplayedResource()) {
                int programIndex = progTerm.getProgramIndex();
                if(programIndex < progTerm.getProgram().getStatements().size())
                    getSourceComponent().addHighlight(programIndex);
            }
        }
    };

}
