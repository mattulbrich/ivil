/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.auto.strategy;

/**
 * This exception is thrown if something goes wrong during application of
 * automatic proof strategies.
 */
@SuppressWarnings("serial") 
public class StrategyException extends Exception {

    public StrategyException() {
        super();
    }

    public StrategyException(String message, Throwable cause) {
        super(message, cause);
    }

    public StrategyException(String message) {
        super(message);
    }

    public StrategyException(Throwable cause) {
        super(cause);
    }

}
