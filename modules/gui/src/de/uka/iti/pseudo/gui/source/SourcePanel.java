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
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.CodeLocation;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

public class SourcePanel extends CodePanel {

    private static final long serialVersionUID = -4642311479594469571L;

    private static final Color SOURCE_COLOR = 
        Settings.getInstance().getColor("pseudo.program.sourcecolor", Color.BLACK);

    public SourcePanel(ProofCenter proofCenter) throws IOException, StrategyException {
        super(proofCenter, true, SOURCE_COLOR);
    }

    @Override
    protected ComboBoxModel getAllResources() {
        Environment env = getProofCenter().getEnvironment();
        Collection<Program> programs = env.getAllPrograms();

        Set<Object> sourceFilenames = new HashSet<Object>();
        for (Program program : programs) {
            URL sourceFile = program.getSourceFile();
            if (sourceFile != null) {
                sourceFilenames.add(sourceFile);
            }
        }

        return new DefaultComboBoxModel(sourceFilenames.toArray());
    }

    @Override
    protected String makeContent(Object reference) {

        if(!(reference instanceof URL) || reference == null)
            return null;

        try {
            return Util.readURLAsString((URL)reference);
        } catch (IOException e) {
            ExceptionDialog.showExceptionDialog(getProofCenter()
                    .getMainWindow(), "Source code cannot be loaded: " + reference, e);
            return null;
        }

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
                Object source = loc.getProgram();
                int sourceLine = loc.getIndex();
                if (source == getDisplayedResource() && sourceLine > 0) {
                    // line numbers start at 1 in code and at 0 in component.
                    getSourceComponent().addHighlight(sourceLine - 1, true);
                }
            }

            if(firstLocs == null && !locs.isEmpty()) {
                firstLocs = locs;
            }
        }

        if(firstLocs != null) {
            for (CodeLocation<?> loc : firstLocs) {
                Object source = loc.getProgram();
                int sourceLine = loc.getIndex();
                if (source == getDisplayedResource() && sourceLine > 0) {
                    // line numbers start at 1 in code and at 0 in component.
                    getSourceComponent().addHighlight(sourceLine - 1, false);
                }
            }
        }
    }

    @Override
    protected Collection<? extends CodeLocation<?>> 
            calculateCodeLocationsOfNode(ProofNode node) {
        return CodeLocation.findSourceCodeLocations(node.getSequent());
    }

}
