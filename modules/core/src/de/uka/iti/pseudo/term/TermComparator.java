/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.util.Log;

/**
 * The Class TermComparator allows for comparison of terms. It implements a
 * total order on all terms.
 * 
 * <h4>Order properties</h4>
 * 
 * For a function symbol <code>f</code> one can specify in a ivil-input-file a
 * property like
 * 
 * <pre>
 * properties
 *   order.f "30"
 * </pre>
 * 
 * in which a natural number is assigned to the function symbol. Accordingly for
 * binder symbols.
 * 
 * <h4>Defaults</h4>
 * 
 * Symbols created through the system have special values. Skolem symbols and
 * other created temporary symbols have the order {@link #CREATED_ORDER} =
 * {@value #CREATED_ORDER}.
 * 
 * Constant symbols (true, 0, 1, 2, ...) have the order {@link #BUILTIN_ORDER} =
 * {@value #BUILTIN_ORDER}.
 * 
 * Other symbols which have not been assigned a value via a property get the
 * order {@link #DEFAULT_ORDER} = {@value #DEFAULT_ORDER}.
 * 
 * <h4>Comparison</h4>
 * 
 * If two terms are compared, first their toplevel symbols are compared. If they
 * differ, decision is made then.
 * 
 * If the two level symbols have equal order, the arity of the symbol comes in,
 * with <b>the term with less subterms having a higher order</b>.
 * 
 * If arities coincide, the comparison goes down to the subterms until one pair
 * of terms differs.
 * 
 * If no difference can be found, the string representations are compared to
 * differentiate as best as possible
 */
public class TermComparator implements Comparator<Term>, TermVisitor {

    /**
     * The default order value for all builtin symbols (true, 0, ...)
     */
    private static final int BUILTIN_ORDER = 100;

    /**
     * The default order value for all created smybols (skolems) 
     */
    private static final int CREATED_ORDER = 0;

    /**
     * The default order value for all symbols not further classified.
     */
    private static final int DEFAULT_ORDER = 50;

    /**
     * The order value for literal program terms (rather low)
     */
    private static final int PROGRAMTERM_ORDER = 10;

    /**
     * The order value for update terms (rather high)
     */
    private static final int UPDATE_ORDER = 80;

    /**
     * The environment used for information lookup
     */
    private Environment env;

    /**
     * The order mapping cache.
     */
    private Map<String, Integer> orderCache = new HashMap<String, Integer>();

    /**
     * Used as result register for the term visitor part of the class 
     */
    private int result;

    /**
     * Instantiates a new term comparator.
     * 
     * @param env
     *            environment to use for information lookup.
     */
    public TermComparator(@NonNull Environment env) {
        this.env = env;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Term t1, Term t2) {

        // determine order value from the toplevel symbol
        int order1 = DEFAULT_ORDER;
        try {
            t1.visit(this);
            order1 = result;
        } catch (TermException e) {
            Log.stacktrace(e);
        }

        int order2 = DEFAULT_ORDER;
        try {
            t2.visit(this);
            order2 = result;
        } catch (TermException e) {
            Log.stacktrace(e);
        }

        // 
        // if they are different, return.
        if (order1 != order2) {
            return order1 - order2;
        }

        //
        // terms with many subterms are smaller than those with few
        // (on same order level!)
        int subtermDiff = t2.countSubterms() - t1.countSubterms();
        if (subtermDiff != 0)
            return subtermDiff;

        //
        // compare subterm by subterm.
        for (int i = 0; i < t1.countSubterms(); i++) {
            int c = compare(t1.getSubterm(i), t2.getSubterm(i));
            if (c != 0) {
                return c;
            }
        }

        //
        // fall back to string comparison
        return t1.toString(false).compareTo(t2.toString(false));
    }

    /**
     * Retrieves the environment that has been associated with this comparator
     * at construction time.
     * 
     * @return the associated environment
     */
    public @NonNull
    Environment getEnvironment() {
        return env;
    }

    /**
     * Gets the order for a certain symbol.
     * 
     * Results are cached.
     * 
     * @param symbol
     *            the token for the symbol
     * @param location
     *            the location for the symbol
     * 
     * @return the order for this symbol.
     */
    private int getOrder(String symbol, ASTLocatedElement location) {

        Integer value = orderCache.get(symbol);
        if (value != null) {
            return value;
        }

        // look in environment
        String property = env.getProperty("order." + symbol);
        if (property != null) {
            try {
                value = Integer.parseInt(property);
                orderCache.put(symbol, value);
                return value;
            } catch (NumberFormatException e) {
                Log.log(Log.ERROR, "An order must be an integer: " + property
                        + " for " + symbol);
            }
        }

        // make default values from location
        if (location == ASTLocatedElement.BUILTIN) {
            value = BUILTIN_ORDER;
        } else if (location == ASTLocatedElement.CREATED) {
            value = CREATED_ORDER;
        } else {
            value = DEFAULT_ORDER;
        }
        
        orderCache.put(symbol, value);
        return value;
    }

    /*
     * variables' order do not really matter since they will never appear
     * toplevel.
     */
    @Override
    public void visit(Variable variable) throws TermException {
        result = DEFAULT_ORDER;
    }

    /*
     * use getOrder to find this value
     */
    @Override
    public void visit(Binding binding) throws TermException {
        Binder binder = binding.getBinder();
        result = getOrder(binder.getName(), binder.getDeclaration());
    }

    /*
     * use getOrder to find this value
     */
    @Override
    public void visit(Application application) throws TermException {
        Function fctSymbol = application.getFunction();
        result = getOrder(fctSymbol.getName(), fctSymbol.getDeclaration());
    }

    /*
     * type variable bindings are considered to be rather on the left ...
     */
    @Override
    public void visit(TypeVariableBinding typeVariableBinding)
            throws TermException {
        result = CREATED_ORDER;
    }

    /*
     * Updated terms have their own order.
     */
    @Override
    public void visit(UpdateTerm updateTerm) throws TermException {
        result = UPDATE_ORDER;
    }

    /*
     * Program have their own order.
     */
    @Override
    public void visit(LiteralProgramTerm literalProgramTerm)
            throws TermException {
        result = PROGRAMTERM_ORDER;
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.uka.iti.pseudo.term.TermVisitor#visit(de.uka.iti.pseudo.term.
     * SchemaProgramTerm)
     */
    @Override
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        throw new TermException(
                "This visitor should not be applied to schema program terms");
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.uka.iti.pseudo.term.TermVisitor#visit(de.uka.iti.pseudo.term.
     * SchemaUpdateTerm)
     */
    @Override
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        throw new TermException(
                "This visitor should not be applied to schema update terms");
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.uka.iti.pseudo.term.TermVisitor#visit(de.uka.iti.pseudo.term.
     * SchemaVariable)
     */
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        throw new TermException(
                "This visitor should not be applied to schema variables");
    }

}
