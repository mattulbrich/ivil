/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import de.uka.iti.pseudo.proof.serialisation.ProofExport;

/**
 * This class is used to filter files of a certain type. The file name extension
 * is used to fiter. This filter wraps a {@link ProofExport} and takes its
 * values from it.
 */
public class ExporterFileFilter extends FileFilter {

    private ProofExport exporter;

    public ExporterFileFilter(ProofExport exporter) {
        this.exporter = exporter;
    }

    public boolean accept(File f) {
        return f.isDirectory()
                || f.getName().endsWith("." + getExporter().getFileExtension());
    }

    public String getDescription() {
        return getExporter().getName();
    }

    public ProofExport getExporter() {
        return exporter;
    }

}
