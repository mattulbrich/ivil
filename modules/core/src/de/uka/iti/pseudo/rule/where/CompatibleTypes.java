package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariableUnification;
import de.uka.iti.pseudo.term.UnificationException;

public class CompatibleTypes extends WhereCondition {

    /**
     * Instantiates a new where condition.
     */
    public CompatibleTypes() {
        super("compatibleTypes");
    }


    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length != 2) {
            throw new RuleException("incompatibleTypes expects exactly two arguments");
        }
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException {

        Type type1 = actualArguments[0].getType();
        Type type2 = actualArguments[1].getType();
        TypeVariableUnification tvu = new TypeVariableUnification();

        try {
            tvu.unify(type1, type2);
            return true;
        } catch (UnificationException e) {
            return false;
        } catch (TermException e) {
            throw new RuleException("Error while checking where condition", e);
        }
    }

}
