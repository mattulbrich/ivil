/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.creation.TypeMatchVisitor;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Binding encapsulates a term with a toplevel one variable binder
 * symbol. There is at least one argument (i.e. child) term.
 * 
 * The arguments are stored as subterms in the superclass, the variable name and
 * type is stored locally. The variable name may also be a schema variable.
 */
public final class Binding extends Term {

    /**
     * The binder toplevel symbol
     */
    private Binder binder;

    /**
     * The bound variable. It is either an instance of {@link Variable} or 
     * {@link SchemaVariable}.
     */
    private BindableIdentifier variable;

    /**
     * Instantiates a Binding.getInst in which the bound variable is represented
     * by a schema variable.
     * 
     * <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     * 
     * @param binder
     *            the binder symbol
     * @param type
     *            the type of the term
     * @param variable
     *            the bound variable as schema variable
     * @param subterms
     *            the argument subterms
     * 
     * @throws TermException
     *             if the type check fails
     */
    private Binding(@NonNull Binder binder, @NonNull Type type,
            @NonNull BindableIdentifier variable,
            @NonNull Term[] subterms) throws TermException {
        super(subterms, type);
        this.binder = binder;
        this.variable = variable;

        typeCheck();
    }
    
    /**
     * Gets an application term.
     * 
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     * 
     * @param binder
     *            the binder symbol
     * @param type
     *            the type of the term
     * @param variable
     *            the bound variable as schema variable
     * @param subterms
     *            the argument subterms
     * 
     * @throws TermException
     *             if the type check fails
     */
    public static @NonNull Binding getInst(@NonNull Binder binder, @NonNull Type type,
            @NonNull BindableIdentifier variable,
            @NonNull Term[] subterms) throws TermException {
        return (Binding) new Binding(binder, type, variable, subterms).intern();
    }

    /**
     * Gets the name of the bound variable.
     * 
     * The name is retrieved by calling <code>toString(false)</code> which returns the
     * name for variables and for schema variables.
     * 
     * @return the variable name
     */
    public @NonNull String getVariableName() {
        return variable.getName();
    }

    /**
     * Gets the type of the bound variable
     * 
     * @return the variable type
     */
    public @NonNull Type getVariableType() {
        return variable.getType();
    }

    /**
     * Gets the bound indentifier which is either a variable or a schema variable
     * 
     * @return the bound identifier
     */
    public @NonNull BindableIdentifier getVariable() {
        return variable;
    }


    /**
     * Gets the binder symbol
     * 
     * @return the binder
     */
    public Binder getBinder() {
        return binder;
    }

    public boolean hasSchemaVariable() {
        return getVariableName().startsWith("%");
    }

    /**
     * Type check this term. 
     * Check typing against all subterms and the bound variable:
     * <ol>
     * <li>check arity
     * <li>check that given the argument terms, the term can be typed
     * <li>and the variable can be typed
     * <li>and that the result type is compatible with the typing result
     * </ol>
     * 
     * @throws TermException
     *             if typing fails at some point
     */
    private void typeCheck() throws TermException {

        if (countSubterms() != binder.getArity()) {
            throw new TermException("Binder " + binder + " expects "
                    + binder.getArity() + " arguments, but got:\n"
                    + Util.listTerms(getSubterms()));
        }

        TypeMatchVisitor matcher = new TypeMatchVisitor(new TermMatcher());
        Type[] argumentTypes = binder.getArgumentTypes();

        try {
            for (int i = 0; i < countSubterms(); i++)
                TypeUnification.makeSchemaVariant(argumentTypes[i]).accept(matcher, getSubterm(i).getType());

            TypeUnification.makeSchemaVariant(binder.getVarType()).accept(matcher, getVariableType());

            TypeUnification.makeSchemaVariant(binder.getResultType()).accept(matcher, getType());
            
        } catch (UnificationException e) {
            throw new TermException("Term " + toString()
                    + " cannot be typed.\nFunction symbol: " + binder
                    + "\nTypes of subterms:\n" + Util.listTypes(getSubterms()), e);
        }

    }

    @Override public @NonNull String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(binder.getName()).append(" ").append(variable.getName());
        
        if(typed) {
            sb.append(" as ").append(variable.getType());
        }
            
        sb.append(";");
        for (int i = 0; i < countSubterms(); i++) {
            sb.append(getSubterm(i).toString(typed));
            if (i != countSubterms() - 1)
                sb.append(";");
        }
        sb.append(")");
        
        if (typed) {
            sb.append(" as ").append(getType());
        }
        return sb.toString();
    }

    @Override public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /*
     * This term is equal to another term if it is a Binding and has the same
     * binder symbol and same arguments.
     * 
     * This method is not invariant to alpha renaming.
     * 
     * TODO implement alpha-invariance?
     */
    @Override public boolean equals(@Nullable Object object) {
        if (object instanceof Binding) {
            Binding bind = (Binding) object;
            if (bind.getBinder() != getBinder())
                return false;

            if (!bind.getType().equals(getType()))
                return false;

            if (!bind.getVariableName().equals(getVariableName()))
                return false;

            if (!bind.getVariableType().equals(getVariableType()))
                return false;

            assert bind.countSubterms() == countSubterms();

            for (int i = 0; i < countSubterms(); i++) {
                if (!bind.getSubterm(i).equals(getSubterm(i)))
                    return false;
            }

            return true;
        }
        return false;
    }
    
    /*
     * This implementation incorporates the binder and variable symbol into the
     * calculation.
     */
    @Override
    protected int calculateHashCode() {
        return super.calculateHashCode() * 31 + (binder.hashCode() ^ variable.hashCode());
    }


}