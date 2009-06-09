package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

// TODO Documentation needed
public class SkolemMetaFunction extends MetaFunction {
    
    private static final String SKOLEM_NAME_PROPERTY = "skolemName";
    
    private static final ASTLocatedElement SKOLEM = new ASTLocatedElement() {
        public String getLocation() { return "SKOLEMISED"; }};

    public SkolemMetaFunction() {
        super(TypeVariable.ALPHA, "$$skolem", TypeVariable.ALPHA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException, ProofException {
        
        String property = SKOLEM_NAME_PROPERTY + "(" + application.getSubterm(0).toString(true) + ")";
        String name = ruleApp.getProperties().get(property);
        if(name == null) {
            if(ruleApp.isMutable()) {
                name = env.createNewFunctionName("sk");
                ruleApp.getProperties().put(property, name);
            } else {
                throw new ProofException("There is no skolemisation stored for " + application);
            }
        }
        
        Function newFunction = new Function(name, application.getType(), new Type[0], 
                false, false, SKOLEM);
    
        try {
            env.addFunction(newFunction);
        } catch (EnvironmentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return new Application(newFunction, application.getType());
    }

}
