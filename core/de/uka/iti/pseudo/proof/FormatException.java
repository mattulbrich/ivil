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
package de.uka.iti.pseudo.proof;

// TODO DOC
// it will be used by other read from string methods as well

public class FormatException extends Exception {
    
    private String kind;
    private String msg;
    private String content;
    
    public FormatException(String kind, String msg, String content) {
        this.kind = kind;
        this.msg = msg;
        this.content = content;
    }
    
    public String getMessage() {
        return "Error in " + kind + ": " + msg + "\nIn: " + content;
    }

}
