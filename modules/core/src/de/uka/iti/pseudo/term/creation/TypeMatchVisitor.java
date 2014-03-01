/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;

/**
 * The Class TypeMatchVisitor implements matching of types in the face of a
 * given term matcher.
 *
 * This class stores assignments to schema types in the term matcher.
 */
public class TypeMatchVisitor extends DefaultTypeVisitor<Type> {

    /**
     * The term matcher to read from / write to.
     */
    private final TermMatcher termMatcher;

    /**
     * Instantiates a new type match visitor.
     *
     * @param termMatcher
     *            the term matcher to use
     */
    public TypeMatchVisitor(TermMatcher termMatcher) {
        this.termMatcher = termMatcher;
    }

    /*
     * try to instantiate the schema variable
     */
    @Override
    public Void visit(SchemaType schemaTypeVariable, Type argument)
            throws TermException {

        String varName = schemaTypeVariable.getVariableName();
        tryInstantiation(varName, argument);
        return null;
    }

    /**
     * Try instantiation for schema variable (given by name). May fail.
     *
     * @param varName
     *            the name of the schema variable
     * @param argument
     *            the type to instantiate it with
     * @throws TermException
     *             if the schema variable is already instantiated with a
     *             different type.
     */
    public void tryInstantiation(String varName, Type argument) throws TermException {

        Type thisSigma = termMatcher.getTypeFor(varName);
        if(thisSigma == null) {
            termMatcher.addTypeInstantiation(varName, argument);
        } else {
            if (!thisSigma.equals(argument)) {
                throw new UnificationException("Incomparable types", thisSigma, argument);
            }
        }
    }

    /*
     * Match type applications.
     */
    @Override
    public Void visit(TypeApplication typeApp, Type argument) throws TermException {
        Sort sort = typeApp.getSort();

        if (argument instanceof TypeApplication) {
            TypeApplication otherApp = (TypeApplication) argument;
            if(sort != otherApp.getSort()) {
                throw new UnificationException("Incomparable sorts", typeApp, argument);
            }

            List<Type> args = typeApp.getArguments();
            List<Type> otherArgs = otherApp.getArguments();
            for(int i = 0; i < sort.getArity(); i++) {
                args.get(i).accept(this, otherArgs.get(i));
            }

        } else {
            throw new UnificationException("Incomparable types", typeApp, argument);
        }
        return null;
    }

    /*
     * Match type variables. Note they cannot be instantiated here, only schema
     * types can be instantiated.
     */
    @Override
    public Void visit(TypeVariable typeVar, Type argument) throws TermException {

        if(!typeVar.equals(argument)) {
            throw new UnificationException("Incomparable types (type var)", typeVar, argument);
        }

        return null;
    }

}