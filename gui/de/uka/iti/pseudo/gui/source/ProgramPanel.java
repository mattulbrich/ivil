/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
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
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.CodeLocation;
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
        // print trace
        for (ProofNode node = proofCenter.getCurrentProofNode().getParent(); null != node; node = node.getParent())
            for (CodeLocation location : node.getSequent().getNativeCodeLocations())
                if (location.getProgram() == getDisplayedResource())
                    if (location.getLine() < ((Program) location.getProgram()).countStatements())
                        getSourceComponent().addHighlight(location.getLine(), true);


        for (CodeLocation location : proofCenter.getCurrentProofNode().getSequent().getNativeCodeLocations())
            if (location.getProgram() == getDisplayedResource())
                if (location.getLine() < ((Program) location.getProgram()).countStatements())
                    getSourceComponent().addHighlight(location.getLine(), false);
    }

    @Override protected Object chooseResource() {
        List<CodeLocation> locations = proofCenter.getCurrentProofNode().getSequent().getNativeCodeLocations();
        if (locations.size() == 0) {
            return null;
        }
        return locations.get(0).getProgram();
    }

    @Override protected ComboBoxModel getAllResources() {
        Collection<Program> programs = getProofCenter().getEnvironment()
                .getAllPrograms();

        return new DefaultComboBoxModel(programs.toArray());
    }

    
}
