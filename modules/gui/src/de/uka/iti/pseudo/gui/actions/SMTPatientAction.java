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


/**
 * The action launches the smt window with a longer timeout.
 */
@SuppressWarnings("serial")
public final class SMTPatientAction extends SMTAction {

    /*
     * Instantiates a new SMT background action.
     */
    public SMTPatientAction() {
        super("patient_smt");
    }

    @Override
    public String getWindowTitle() {
        return "Applying the SMT solver patiently";
    }

}


