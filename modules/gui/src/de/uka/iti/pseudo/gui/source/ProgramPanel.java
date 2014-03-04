/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
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

    public ProgramPanel(ProofCenter proofCenter) throws IOException, StrategyException {
        super(proofCenter, false, PROGRAM_COLOR);
    }

    @Override
    protected String makeContent(Object object) {

        if (prettyPrinter == null) {
            prettyPrinter = getProofCenter().getPrettyPrinter();
            prettyPrinter.addPropertyChangeListener(this);
        }

        if (object == null) {
            return null;
        }

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
            sb.append(String.format("%3d|%3s: %s%s%n", i, linestring,
                    prettyPrinter.print(statement).toString(),
                    annotation == null ? "" : " ; " + annotation));
        }
        return sb.toString();
    }

    @Override
    protected void addHighlights() {
        // print trace
        // remember the first parent that has a location
        Collection<? extends CodeLocation<?>> firstLocs = null;
        for (ProofNode node = proofCenter.getCurrentProofNode();
                node != null; node = node.getParent()) {
            Collection<? extends CodeLocation<?>> locs = getCodeLocations(node);

            for (CodeLocation<?> loc : locs) {
                if(loc.getProgram() == getDisplayedResource() &&
                        loc.getIndex() < ((Program) loc.getProgram()).countStatements()) {
                    getSourceComponent().addHighlight(loc.getIndex(), true);
                }
            }

            if(firstLocs == null && !locs.isEmpty()) {
                firstLocs = locs;
            }
        }

        if(firstLocs != null) {
            for (CodeLocation<?> loc : firstLocs) {
                if(loc.getProgram() == getDisplayedResource() &&
                        loc.getIndex() < ((Program) loc.getProgram()).countStatements()) {
                    getSourceComponent().addHighlight(loc.getIndex(), false);
                }
            }
        }
    }

    @Override
    protected Collection<? extends CodeLocation<?>> calculateCodeLocationsOfNode(ProofNode node) {
        return CodeLocation.findCodeLocations(node.getSequent());
    }

    @Override protected ComboBoxModel getAllResources() {
        Collection<Program> programs = getProofCenter().getEnvironment()
                .getAllPrograms();

        return new DefaultComboBoxModel(programs.toArray());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // if the source of the property change is not the pretty printer, use
        // the generic handler
        if (!evt.getSource().equals(prettyPrinter)) {
            super.propertyChange(evt);
        }

        selectSource();
    }
}
