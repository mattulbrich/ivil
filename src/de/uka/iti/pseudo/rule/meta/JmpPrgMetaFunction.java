package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

/**
 * This function changes the program pointer in a literal program term without
 * changing the remaining information within the program term (i.e. the program
 * to which it references)
 */
public class JmpPrgMetaFunction extends MetaFunction {
    
    private static final Type BOOL = Environment.getBoolType();
    private static final Type INT= Environment.getIntType();
    
    public JmpPrgMetaFunction() {
        super(BOOL, "$$jmpPrg", BOOL, INT);
    }

    @Override 
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        Term target = application.getSubterm(1);
        int jmpTarget = -1;
        
        if (target instanceof Application) {
            Function f = ((Application) target).getFunction();
            if (f instanceof NumberLiteral) {
                NumberLiteral literal = (NumberLiteral) f;
                jmpTarget = literal.getValue().intValue();
            }
        }
        
        if(jmpTarget == -1)
            throw new TermException("jmp target is not an integer literal: " + target);
        
        if (arg instanceof LiteralProgramTerm) {
            LiteralProgramTerm progTerm = (LiteralProgramTerm) arg;
            return new LiteralProgramTerm(jmpTarget, progTerm);
        } else {
            throw new TermException("not a program term " + arg);
        }
        
    }
}
