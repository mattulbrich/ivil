/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;

/**
 * This where condition can be used in rules to indicate that a schema variable
 * should be instantiated interactively.
 *
 * <h4>One parameter</h4> The where condition places a property into the rule
 * application indicating that the schema variable <code>%v</code> of type
 * <code>T</code> is to be interactively instantiated. It is of the form
 *
 * <pre>
 *   interact(%v) : T
 * </pre>
 *
 * It is then the task of the interaction to process these properties and to
 * amend the schema variable mapping of the rule application.
 *
 * <h4>Two parameters (type mode)</h4> The where condition can also carry a second parameter
 * which must either be <code>true</code> or <code>false</code> (literally).
 * <code>false</code> indicates the usual behaviour while <code>true</code> also
 * allows to instantiate the type of the schema variable (<i>which <b>must</b> be
 * schema type then). This is convenient for type instantiations.
 *
 * @author mattias ulbrich
 */
public class Interactive extends WhereCondition {

    /**
     * The name of the condition and the property.
     */
    public static final String INTERACTION = "interact";
    public static final String INSTANTIATE_PREFIX = "instantiate ";

    /**
     * Instantiates a new interactive.
     */
    public Interactive() {
        super(INTERACTION);
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.environment.WhereCondition#checkSyntax(de.uka.iti.pseudo.term.Term[])
     */
    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 2 || arguments.length == 0) {
            throw new RuleException("interact expects 1 or 2 arguments");
        }

        Term arg1 = arguments[0];
        if(!(arg1 instanceof SchemaVariable)) {
            throw new RuleException("interact expects schema varible as first argument");
        }

        isTypeMode(arguments);
    }

    private boolean isTypeMode(Term[] arguments) throws RuleException {
        if(arguments.length == 2) {
            Term arg2 = arguments[1];
            if(arg2.equals(Environment.getTrue())) {
                Term arg1 = arguments[0];
                if(!(arg1.getType() instanceof SchemaType)) {
                    throw new RuleException("in type mode: 1st argument must be of schema type");
                }

                return true;

            } else if(!arg2.equals(Environment.getFalse())) {
                throw new RuleException("second argument must be either literal 'true' or 'false'");
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.environment.WhereCondition#check(de.uka.iti.pseudo.term.Term[], de.uka.iti.pseudo.term.Term[], de.uka.iti.pseudo.proof.RuleApplication, de.uka.iti.pseudo.proof.ProofNode, de.uka.iti.pseudo.environment.Environment)
     */
    @Override
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {
        if(ruleApp.hasMutableProperties()) {
            SchemaVariable sv = (SchemaVariable) formalArguments[0];
            Term actualTerm = actualArguments[0];

            String typeString = actualTerm.getType().toString();;
            if(isTypeMode(formalArguments)) {
                typeString = INSTANTIATE_PREFIX + typeString;
            }

            ruleApp.getProperties().put(INTERACTION + "(" + sv.getName() + ")",
                    typeString);
        }
        return true;
    }

}
