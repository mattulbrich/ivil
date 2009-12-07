/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.justify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import nonnull.NonNull;

import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

/**
 * The Class SchemaVariableUseVisitor can be used to analyse the schema
 * variables in a term.
 * <p>
 * The visitation fills in 2 data elements:
 * <dd>
 * <dt>{@link #schemaVarsUsedInBindings}</dt>
 * <dl>
 * a set of schemavariables which are used as bound variables in bindings
 * </dl>
 * <dt>{@link #seenBindables}
 * <dl>
 * a mapping from all appearing schema variables to the set of bound variables
 * (or schema variables) which are bound in <b>all</b> appearances of the schema
 * variable.
 * </dl>
 * </dd>
 */
public class SchemaVariableUseVisitor extends DefaultTermVisitor.DepthTermVisitor {
	
	/**
	 * A mapping from schema variables to the set of bound variables (or schema
	 * variables) which are bound in <b>all</b> appearances of the schema
	 * variable.
	 * 
	 * The value is always a valid object and never <code>null</code>.
	 */
	private Map<SchemaVariable, Set<BindableIdentifier>> seenBindables =
		new HashMap<SchemaVariable, Set<BindableIdentifier>>();

	
	/**
	 * The current bindings to build {@link #seenBindables}.
	 */
	private Stack<BindableIdentifier> currentBindings =
		new Stack<BindableIdentifier>();
	
	/**
	 * The set of all identifiers which are bound in bindings in the term
	 */
	private Set<BindableIdentifier> boundIdentifiers =
		new HashSet<BindableIdentifier>();
	
	/**
	 * Gets the set of identifiers which have been used in bindings.
	 * 
	 * @return the set of bound identifiers
	 */
	public @NonNull Set<BindableIdentifier> getBoundIdentifiers() {
		return boundIdentifiers;
	}
	
	/**
	 * Gets a mapping from schema variables to sets of those bindable
	 * identifiers which are observed in every appearance. 
	 * 
	 * @return a map with non-null values.
	 */
	public @NonNull Map<SchemaVariable, Set<BindableIdentifier>> getSeenBindablesMap() {
		return seenBindables;
	}

	@Override
	public void visit(Binding binding) throws TermException {
		BindableIdentifier bindable = binding.getVariable();
		currentBindings.push(bindable);
		boundIdentifiers.add(bindable);
		super.visit(binding);
		currentBindings.pop();
	}
	
	@Override
	public void visit(SchemaVariable schemaVariable) throws TermException {
		Set<BindableIdentifier> seen = seenBindables.get(schemaVariable);
		if(seen == null) {
			seenBindables.put(schemaVariable, new HashSet<BindableIdentifier>(currentBindings));
		} else {
			seen.retainAll(currentBindings);
		}

	}
	
}