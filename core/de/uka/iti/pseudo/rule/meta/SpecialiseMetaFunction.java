package de.uka.iti.pseudo.rule.meta;

import static de.uka.iti.pseudo.term.TypeVariable.ALPHA;
import static de.uka.iti.pseudo.term.TypeVariable.BETA;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;

public class SpecialiseMetaFunction extends MetaFunction {

    private static final Type GAMMA = new TypeVariable("gamma");
    private static final Type DELTA = new TypeVariable("delta");
    private static final Type BOOL = Environment.getBoolType();

    public SpecialiseMetaFunction() {
        //       toReplace, replaceWith, replaceIn
        super(BOOL, "$$specialiseType", ALPHA, BETA, BOOL);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Type toReplace = application.getSubterm(0).getType();
        Type replaceWith = application.getSubterm(1).getType();
        Term replaceIn = application.getSubterm(2);
        
        if(!(toReplace instanceof TypeVariable)) {
            throw new TermException("Only type variables can be specialised, but not: " + toReplace);
        }
        
        TypeVariable typeVar = (TypeVariable) toReplace;
        
        SchemaTypeInstantiator tr = new SchemaTypeInstantiator();
        return tr.replace(typeVar, replaceWith, replaceIn);
        
    }
    
    static class SchemaTypeInstantiator extends RebuildingTermVisitor {
        
        private TypeVariable tyOfInterest;
        private Type instantiation;
        
        private RebuildingTypeVisitor<Void> typeVariableReplacer = new RebuildingTypeVisitor<Void>() {
            @Override
            public Type visit(TypeVariable typeVariable, Void parameter) throws TermException {
                if(typeVariable.equals(tyOfInterest)) {
                    return instantiation;
                } else {
                    return typeVariable;
                }
            }
        };

        @Override
        protected Type modifyType(Type type) throws TermException {
            return type.accept(typeVariableReplacer, null);
        }
        
        public Term replace(TypeVariable typeVar, Type replaceWith, Term replaceIn) throws TermException {
        
            tyOfInterest = typeVar;
            instantiation = replaceWith;
            
            replaceIn.visit(this);
            return resultingTerm == null ? replaceIn : resultingTerm;
        }
        
    }
    

}
