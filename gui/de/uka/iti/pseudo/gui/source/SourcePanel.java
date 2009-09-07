package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.ProofNodeSelectionListener;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.settings.Settings;

public class SourcePanel extends ChoosePanel implements
        ProofNodeSelectionListener {

    private static final Color SOURCE_COLOR = Settings.getInstance()
            .getColor("pseudo.program.sourcecolor");

    public SourcePanel(ProofCenter proofCenter) throws IOException {
        super(proofCenter, true, SOURCE_COLOR);
    }

    @Override
    protected ComboBoxModel updatePrograms() {
        Environment env = getProofCenter().getEnvironment();
        Collection<Program> programs = env.getAllPrograms();
        File resource = new File(env.getResourceName());

        Set<Object> sourceFilenames = new HashSet<Object>();
        for (Program program : programs) {
            String sourceFile = program.getSourceFile();
            if (sourceFile != null) {
                File f = new File(resource.getParentFile(), sourceFile);
                if(f.canRead())
                    sourceFilenames.add(f);
                else
                    sourceFilenames.add(f + " - cannot be read");
            }
        }

        return new DefaultComboBoxModel(sourceFilenames.toArray());
    }

    @Override
    protected String makeContent(Object reference) {
        
        if(!(reference instanceof File)) {
            return "";
        }
        
        FileReader reader = null;
        try {
            StringBuilder result = new StringBuilder();
            
            reader = new FileReader((File)reference);
            char[] buffer = new char[2048];

            int read = reader.read(buffer);
            while (read > 0) {
                result.append(buffer, 0, read);
                read = reader.read(buffer);
            }

            return result.toString();

        } catch (IOException e) {
            ExceptionDialog.showExceptionDialog(getProofCenter()
                    .getMainWindow(), e);
            return "";

        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                }
        }

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
        public void visit(LiteralProgramTerm progTerm) {

            String sourceName = progTerm.getProgram().getSourceFile();
            if(sourceName != null) {
                File res = new File(getProofCenter().getEnvironment().
                        getResourceName()).getParentFile();
                File source = new File(res, sourceName);
                if(source.equals(getDisplayedResource())) {
                    int index = progTerm.getStatement().getSourceLineNumber();
                    if(index != -1)
                        getSourceComponent().addHighlight(index);
                }
            }
        }};

}
