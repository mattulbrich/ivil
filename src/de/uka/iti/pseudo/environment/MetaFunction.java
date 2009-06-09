package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.meta.SkolemMetaFunction;
import de.uka.iti.pseudo.rule.meta.SubstMetaFunction;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation needed
public abstract class MetaFunction extends Function {
    
    // eventually do this with the services and provider stuff
    public static final MetaFunction META_FUNCTIONS[] = {
        new SubstMetaFunction(),
        new SkolemMetaFunction()
    };

    public MetaFunction(Type resultType, String name, Type... argumentTypes) {
        super(name, resultType, argumentTypes, false, false, ASTLocatedElement.BUILTIN);
        
        assert name.startsWith("$$");
    }
    
    
    public abstract Term evaluate(Application application, Environment env, RuleApplication ruleApp)
       throws TermException;

}
