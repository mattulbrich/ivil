package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

public class SourcePanel extends CodePanel {

    private static final Color SOURCE_COLOR = Settings.getInstance()
            .getColor("pseudo.program.sourcecolor");

    public SourcePanel(ProofCenter proofCenter) throws IOException {
        super(proofCenter, true, SOURCE_COLOR);
    }

    @Override
    protected ComboBoxModel getAllResources() {
        Environment env = getProofCenter().getEnvironment();
        Collection<Program> programs = env.getAllPrograms();

        Set<Object> sourceFilenames = new HashSet<Object>();
        for (Program program : programs) {
            File sourceFile = program.getSourceFile();
            if (sourceFile != null) {
                if(sourceFile.canRead())
                    sourceFilenames.add(sourceFile);
                else
                    sourceFilenames.add(sourceFile + " - cannot be read");
            }
        }

        return new DefaultComboBoxModel(sourceFilenames.toArray());
    }

    @Override
    protected String makeContent(Object reference) {
        
        if(!(reference instanceof File) || reference == null)
            return null;
        
        try {
            return Util.readFileAsString((File)reference);
        } catch (IOException e) {
            ExceptionDialog.showExceptionDialog(getProofCenter()
                    .getMainWindow(), e);
            return null;
        }

    }
    
    @Override protected void addHighlights() {
        for (LiteralProgramTerm progTerm : getFoundProgramTerms()) {
            File source = progTerm.getProgram().getSourceFile();
            Object displayedResource = getDisplayedResource();
            if (source != null && source.equals(displayedResource)) {
                int sourceLine = progTerm.getStatement().getSourceLineNumber();
                if (sourceLine > 0) {
                    // line numbers start at 1 in code and at 0 in component.
                    getSourceComponent().addHighlight(sourceLine - 1);
                }
            }
        }
    }

    @Override protected Object chooseResource() {
        for (LiteralProgramTerm progTerm : getFoundProgramTerms()) {
            File source = progTerm.getProgram().getSourceFile();
            // TODO is this sane? Better check for entries in choice box.
            if (source != null && source.canRead()) {
                return source;
            }
        }
        return null;
    }

}
