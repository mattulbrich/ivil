package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.settings.Settings;

public class ProgramPanel extends CodePanel {

    private static final long serialVersionUID = 310718223333L;

    private static final Color PROGRAM_COLOR = 
        Settings.getInstance().getColor("pseudo.program.boogiecolor", Color.BLACK);

    private PrettyPrint prettyPrinter;

    public ProgramPanel(ProofCenter proofCenter) throws IOException {
        super(proofCenter, false, PROGRAM_COLOR);
    }

    protected String makeContent(Object object) {

        if (prettyPrinter == null)
            this.prettyPrinter = getProofCenter().getPrettyPrinter();

        if (object == null)
            return null;

        Program p = (Program) object;
        StringBuilder sb = new StringBuilder();
        List<Statement> statements = p.getStatements();
        List<String> annotations = p.getTextAnnotations();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String annotation = annotations.get(i);
            int sourceline = statement.getSourceLineNumber();
            String linestring = sourceline > 0 ? Integer.toString(sourceline)
                    : "";
            sb.append(String.format("%3d|%3s: %s%s\n", i, linestring,
                    prettyPrinter.print(statement).toString(),
                    annotation == null ? "" : " ; " + annotation));
        }
        return sb.toString();
    }

    @Override protected void addHighlights() {
        List<LiteralProgramTerm> progTerms = getFoundProgramTerms();
        for (LiteralProgramTerm progTerm : progTerms) {
            Program program = progTerm.getProgram();
            if(program == getDisplayedResource()) {
                int index = progTerm.getProgramIndex();
                if (index < program.countStatements())
                    getSourceComponent().addHighlight(index);
            }
        }
    }

    @Override protected Object chooseResource() {
        List<LiteralProgramTerm> progTerms = getFoundProgramTerms();
        if(progTerms.isEmpty()) {
            return null;
        }
        return progTerms.get(0).getProgram();
    }

    @Override protected ComboBoxModel getAllResources() {
        Collection<Program> programs = getProofCenter().getEnvironment()
                .getAllPrograms();

        return new DefaultComboBoxModel(programs.toArray());
    }

    
}
