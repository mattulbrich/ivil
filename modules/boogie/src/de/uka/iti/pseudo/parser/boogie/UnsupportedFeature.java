/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie;

/**
 * This exception shall be used to flag unsupported features. The class has to
 * be deleted before marking the Boogie Parser as stable.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class UnsupportedFeature extends ParseException {
    private static final long serialVersionUID = -4775885357976000017L;

    public UnsupportedFeature(String msg) {
        super(msg);
    }
}
