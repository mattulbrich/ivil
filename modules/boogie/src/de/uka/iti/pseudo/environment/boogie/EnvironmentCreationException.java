/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.boogie;

import de.uka.iti.pseudo.parser.boogie.ParseException;

/**
 * This exception indicates an unexpected error during environment creation.
 * These should mostly point to a bug in the environment creation process
 * itself.
 * 
 * @author timm.felden@felden.com
 */
public class EnvironmentCreationException extends ParseException {

    private static final long serialVersionUID = 9217770502098071890L;

    public EnvironmentCreationException(String string) {
        super(string);
    }

    public EnvironmentCreationException(String string, Exception cause) {
        super(string, cause);
    }

}