package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.term.Term;

public final class TranslationPhase {

    /**
     * Name used in ivil; this is necessary, as ivil has only one global
     * namespace.
     */
    public final Map<Variable, String> variableNames = new HashMap<Variable, String>();
    public final Map<Expression, Term> terms = new HashMap<Expression, Term>();

    public void create(EnvironmentCreationState state) throws EnvironmentCreationException {

        new ProgramMaker(state);
    }

}
