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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.ToplevelCheckVisitor;

/**
 * The Class Lemma encapsulates a lemma definition within an environment.
 *
 * It essentially consists of a name and a boolean toplevel term.
 *
 * Properties may be attached to a lemma. In particular, the property "axiom" is
 * added if the lemma is declared as an axiom.
 *
 * Lemmas (which are not axioms) give rise to {@link ProofObligation}s.
 */
public final class Lemma implements Named {

    /**
     * The name of this axiom.
     */
    private final @NonNull String name;

    /**
     * The properties.
     */
    private final @DeepNonNull Map<String, String> properties;

    /**
     * The axiom formula.
     */
    private final @NonNull Term term;

    /**
     * The definition location of the axiom.
     */
    private final @NonNull ASTLocatedElement location;

    /**
     * Instantiates a new lemma.
     *
     * <P>
     * The term must be boolean and
     *
     * @param name
     *            name of the lemma
     * @param term
     *            the term
     * @param properties
     *            the properties
     * @param location
     *            the location at which the definition resides
     * @throws EnvironmentException
     *             if the term is not suited for this purpose
     */
    public Lemma(@NonNull String name, @NonNull Term term,
            @NonNull Map<String, String> properties,
            @NonNull ASTLocatedElement location) throws EnvironmentException {
        super();
        this.name = name;
        this.properties = new HashMap<String, String>(properties);
        this.term = term;
        this.location = location;

        ToplevelCheckVisitor tcv = new ToplevelCheckVisitor();
        try {
            term.visit(tcv);
        } catch (TermException e) {
            throw new EnvironmentException("Lemma '" + name + "' is not top a level term", e);
        }
    }

    /**
     * Gets a property. Properties are specified using the "tag" keyword in
     * environments. If the property is not set, null is returned. If the
     * property has been defined without a value, an empty string "" is returned
     *
     * <p>Please use a constant defined in {@link RuleTagConstants} as argument
     * to keep all sensible tags at one place.
     *
     * @see RuleTagConstants
     *
     * @param string
     *            name of the property to retrieve
     * @return the property if it is defined, null otherwise
     */
    public @Nullable String getProperty(String string) {
        return properties.get(string);
    }

    /**
     * Gets a collection which contains the names of all defined properties for this
     * rule. The entries to this collections are different from null and can be used
     * as keys to {@link #getProperty(String)}.
     *
     * @return an unmodifiable collection of strings.
     */
    public Collection<String> getDefinedProperties() {
        return properties.keySet();
    }


    /**
     * Gets the name of this lemma.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Axiom[" + name + "]";
    }

    public ASTLocatedElement getDeclaration() {
        return location;
    }

    /**
     * Gets the formula of this lemma.
     *
     * It is a formula which can be added to a sequent (no schema vars, free
     * variables etc)
     *
     * @return the term for this lemma
     */
    public @NonNull Term getTerm() {
        return term;
    }

}
