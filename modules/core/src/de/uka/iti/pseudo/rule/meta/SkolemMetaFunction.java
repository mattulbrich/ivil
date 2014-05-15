/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.LocalSymbolTable;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

/**
 * This meta function can be used to create a new skolem function symbol in a
 * rule. It stores its name and type into a property in the
 * {@link RuleApplication}.
 *
 * @ivildoc "Meta function/$$skolem"
 *
 * <h2>Meta function <tt>$$skolem</tt></h2>
 *
 * This meta function can be used to create a new skolem symbol. Since ivil does
 * not support free variables (unbound) every skolem symbol is nullary function
 * symbol.
 *
 * <h3>Syntax</h3>
 *
 * The meta function takes one argument of arbitrary type and returns a term of
 * the same type. If applied twice or more often to the same term, it results in
 * the same term. This term is new constant symbol which has not yet occurred in
 * the proof.
 *
 * <h3>Example:</h3>
 *
 * <pre>
 * rule forall_right
 *   find  |-  (\forall %x; %b)
 *   replace  $$subst(%x, $$skolem(%x), %b)
 * </pre>
 *
 * <h3>See also:</h3>
 * <a href="ivil:/Meta function/$$subst">$$subst</a>
 *
 * @author mattias ulbrich
 */
public class SkolemMetaFunction extends MetaFunction {

    public static final String SKOLEM_NAME_PROPERTY = "skolemName";

    public static final ASTLocatedElement SKOLEM = new ASTLocatedElement() {
        @Override
        public String getLocation() { return "SKOLEMISED"; }};

    public SkolemMetaFunction() throws EnvironmentException {
        super(TypeVariable.ALPHA, "$$skolem", TypeVariable.ALPHA);
    }

    @Override
    public Term evaluate(Application application, MetaEvaluator metaEval) throws TermException {

        RuleApplication ruleApp = metaEval.getRuleApplication();
        Environment env = metaEval.getEnvironment();
        LocalSymbolTable lst = metaEval.getLocalSymbolTable();

        String property = SKOLEM_NAME_PROPERTY + "(" + application.getSubterm(0).toString(true) + ")";
        String name = ruleApp.getProperties().get(property);
        if(name == null) {
            if(ruleApp.hasMutableProperties()) {
                name = calcSkolemName(application, env);
                ruleApp.getProperties().put(property, name);
            } else {
                throw new TermException("There is no skolemisation stored for " + application);
            }
        }

        Function newFunction = env.getFunction(name);
        if(newFunction == null) {
            try {
                newFunction = new Function(name, application.getType(), new Type[0],
                    false, false, SKOLEM);

                lst.addFunction(newFunction);
            } catch (EnvironmentException e) {
                throw new TermException(e);
            }
        }

        return Application.getInst(newFunction, application.getType());
    }


    /*
     * If the skolemised term is either a variable or a function symbol
     * use its name as prefix. Otherwise fall back to "sk"
     */
    private String calcSkolemName(Application application, Environment env) {
        String prefix = "sk";
        Term term = application.getSubterm(0);
        if (term instanceof Application) {
            // try to use function symbol name to skolemise
            Function innerFunct = ((Application) term).getFunction();
            if(!(innerFunct instanceof NumberLiteral)) {
                prefix = innerFunct.getName();
            }

        } else if (term instanceof Variable) {
            Variable var = (Variable) term;
            prefix = var.getName();
        }

        return env.createNewFunctionName(prefix);
    }

}
