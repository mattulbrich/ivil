/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.util.ExceptionDialog;

@SuppressWarnings("serial")
public class PopupOpenErrorFile
    extends BarAction
    implements InitialisingAction, PropertyChangeListener{

    private String errorFile;

    public PopupOpenErrorFile() {
        super("Open erroneous file");
        putValue(SHORT_DESCRIPTION, "open the file containing the error in a new window");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(errorFile == null) {
            return;
        }
        try {
            URL url = new URL(errorFile);
            if(!"file".equals(url.getProtocol())) {
                throw new IOException(url.getProtocol() +
                        ": Only the file protocol is supported for this functionality");
            }
            URI uri = url.toURI();
            File file = new File(uri.getPath());
            Main.openEditor(file);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }

    @Override
    public void initialised() {
        getEditor().addPropertyChangeListener("errorFile", this);
        setEnabled(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        errorFile = (String) evt.getNewValue();
        setEnabled(errorFile != null);
    }

}
