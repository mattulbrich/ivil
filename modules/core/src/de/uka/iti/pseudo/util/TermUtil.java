package de.uka.iti.pseudo.util;

import java.math.BigInteger;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * The Class TermUtil is a collection of static methods which can be used to
 * analyse and or modify {@link Term} objects.
 */
public final class TermUtil {

    private static final BigInteger MAXINT_BIGINT =
            BigInteger.valueOf(Integer.MAX_VALUE);

    private TermUtil() {
        assert false : "must not be instantiated";
    }

    /**
     * Extract the integer value from an integer literal.
     *
     * If the value
     *
     * @param value
     *            the term which must be an integer literal
     * @return the int literal
     * @throws TermException
     *             if the term is not an integer literal
     */
    public static int getIntLiteral(@NonNull Term value) throws TermException {
        if (!(value instanceof Application)) {
            throw new TermException("expected a number literal, not " + value);
        }

        Function func = ((Application) value).getFunction();
        if (!(func instanceof NumberLiteral)) {
            throw new TermException("expected a number literal, not " + value);
        }

        BigInteger bigIntValue = ((NumberLiteral) func).getValue();
        if (bigIntValue.compareTo(MAXINT_BIGINT) > 0) {
            throw new TermException("The integer literal " + value +
                    " is too large for a 32 bit integer");
        }

        int intValue = bigIntValue.intValue();
        return intValue;
    }

    /**
     * Gets the function symbol of a function application.
     *
     * The argument must be a function application or an exception will be
     * thrown.
     *
     * @param value
     *            the term to analyse
     * @return the function symbol of the function application
     * @throws TermException
     *             if the argument is not a function application
     */
    public static Function getFunction(@NonNull Term value) throws TermException {
        if (!(value instanceof Application)) {
            throw new TermException("expected an application term, not " + value);
        }
        return ((Application) value).getFunction();
    }

    /**
     * Checks if a term is q univeral quantification.
     *
     * @param term
     *            the term to check
     * @return true, if is a universal quantification
     */
    public static boolean isForall(Term term) {
        if (term instanceof Binding) {
            Binding binding = (Binding) term;
            return "\\forall".equals(binding.getBinder().getName());
        }
        return false;
    }

    /**
     * C Checks if a term is an existential quantification.
     *
     * @param term
     *            the term to check
     * @return true, if is an existential quantification
     */
    public static boolean isExists(Term term) {
        if (term instanceof Binding) {
            Binding binding = (Binding) term;
            return "\\exists".equals(binding.getBinder().getName());
        }
        return false;
    }

    /**
     * Checks if a term is an equality.
     *
     * @param term
     *            the term to check
     * @return true, iff it is an equality
     */
    public static boolean isEquality(Term term) {
        return isFunctionApplication(term, "$eq");
    }

    /**
     * Checks if a term is a conjunction.
     *
     * @param term
     *            the term to check
     * @return true, iff term is a conjunction
     */
    public static boolean isConjunction(Term term) {
        return isFunctionApplication(term, "$and");
    }

    /**
     * Checks if a term is a function application of a certain function symbol.
     *
     * @param term
     *            the term to check
     * @param functionName
     *            the function name to check
     * @return true, iff the argument is an application of the function symbol
     */
    public static boolean isFunctionApplication(Term term, String functionName) {
        if (term instanceof Application) {
            Application app = (Application) term;
            return functionName.equals(app.getFunction().getName());
        }
        return false;
    }

    /**
     * Checks if a term is a function application of a certain function symbol.
     *
     * @param term
     *            the term to check
     * @param functionName
     *            the function symbol to check
     * @return true, iff the argument is an application of the function symbol
     */
    public static boolean isFunctionApplication(Term term, Function function) {
        if (term instanceof Application) {
            Application app = (Application) term;
            return function == app.getFunction();
        }
        return false;
    }
}
