/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import java.math.BigInteger;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;

/**
 * A NumberLiteral is a special kind of nullary function symbol
 * holding a natural number.
 *
 * Operations on number literals are handled through this class.
 */
public class NumberLiteral extends Function {

    /**
     * The Constant NO_ARGS is kept for the
     * super constructor call.
     */
    public static final Type[] NO_ARGS = new Type[0];

    /**
     * The value of the number literal.
     */
    private final BigInteger value;

    /**
     * Instantiates a new number literal.
     *
     * @param value the value of the number
     * @param env the environment to rely upon
     *
     * @throws EnvironmentException if the the number is negative
     */
    public NumberLiteral(BigInteger value, Environment env)
            throws EnvironmentException {
        super(value.toString(), Environment.getIntType(), NO_ARGS, true, false,
                ASTLocatedElement.BUILTIN);

        this.value = value;

        if(value.signum() == -1) {
            throw new EnvironmentException("A number literal must not be negative: " + value);
        }
    }

    /**
     * Gets the numeric value of this literal.
     *
     * @return the numeric value of this literal
     */
    public @NonNull BigInteger getValue() {
        return value;
    }

}
