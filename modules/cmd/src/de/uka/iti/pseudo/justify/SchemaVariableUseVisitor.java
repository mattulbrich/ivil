/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.justify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

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
	private @NonNull Map<SchemaVariable, SortedSet<BindableIdentifier>> seenBindables =
		new HashMap<SchemaVariable, SortedSet<BindableIdentifier>>();

	
	/**
	 * The current bindings to build {@link #seenBindables}.
	 */
	private Stack<BindableIdentifier> currentBindings =
		new Stack<BindableIdentifier>();
	
	/**
	 * The set of all identifiers which are bound in bindings in the term
	 */
	private SortedSet<BindableIdentifier> boundIdentifiers =
		new TreeSet<BindableIdentifier>();
	
	/**
	 * Gets the set of identifiers which have been used in bindings.
	 * 
	 * @return the set of bound identifiers
	 */
	public @NonNull SortedSet<BindableIdentifier> getBoundIdentifiers() {
		return boundIdentifiers;
	}
	
	/**
	 * Gets a mapping from schema variables to sets of those bindable
	 * identifiers which are observed in every appearance. 
	 * 
	 * @return a map with non-null values.
	 */
	public @NonNull Map<SchemaVariable, SortedSet<BindableIdentifier>> getSeenBindablesMap() {
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
			seenBindables.put(schemaVariable, new TreeSet<BindableIdentifier>(currentBindings));
		} else {
			seen.retainAll(currentBindings);
		}

	}
	
}