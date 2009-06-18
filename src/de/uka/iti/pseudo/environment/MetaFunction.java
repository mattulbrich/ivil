package de.uka.iti.pseudo.environment;

import java.util.ServiceLoader;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation badly needed
public abstract class MetaFunction extends Function {
    
    public static final ServiceLoader<MetaFunction> SERVICES =
        ServiceLoader.load(MetaFunction.class);

    public MetaFunction(Type resultType, String name, Type... argumentTypes) {
        super(name, resultType, argumentTypes, false, false, ASTLocatedElement.BUILTIN);
        
        assert name.startsWith("$$");
    }
    
    
    public abstract Term evaluate(Application application, Environment env, RuleApplication ruleApp)
       throws TermException;

}
