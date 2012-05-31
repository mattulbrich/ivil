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
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.ToplevelCheckVisitor;

/**
 * The Class Axiom encapsulates an axiom definition within an environment.
 *
 * It essentially has a name and a boolean toplevel term.
 */
public final class Axiom {

    /**
     * The name of this axiom.
     */
    private final String name;

    /**
     * The properties.
     */
    private final Map<String, String> properties;

    /**
     * The axiom formula.
     */
    private final Term term;

    /**
     * The definition location of the axiom.
     */
    private final ASTLocatedElement location;

    /**
     * Instantiates a new axiom.
     *
     * <P>The term must be boolean and
     *
     * @param name
     *            name of the axiom
     * @param term
     *            the term
     * @param properties
     *            the properties
     * @param location
     *            the location
     * @throws EnvironmentException
     *             the environment exception
     */
    public Axiom(@NonNull String name, @NonNull Term term,
            @NonNull Map<String, String> properties,
            @NonNull ASTLocatedElement location) throws EnvironmentException {
        super();
        this.name = name;
        this.properties = properties;
        this.term = term;
        this.location = location;

        ToplevelCheckVisitor tcv = new ToplevelCheckVisitor();
        try {
            term.visit(tcv);
        } catch (TermException e) {
            throw new EnvironmentException("Axiom is not top level term", e);
        }
    }

    /**
     * gets a property. Properties are specified using the "tag" keyword in
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
     * Gets the name of this axiom.
     *
     * @return the name
     */
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

    public Term getTerm() {
        return term;
    }

}
