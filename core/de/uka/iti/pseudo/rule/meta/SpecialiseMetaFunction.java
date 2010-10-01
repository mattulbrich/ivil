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

    private static final Type BOOL = Environment.getBoolType();

    public SpecialiseMetaFunction() {
        //       toReplace, replaceWith, replaceIn, replaceTermAndType
        super(BOOL, "$$polymorphicSpec", ALPHA, BETA, BOOL, BOOL);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term termToReplace = application.getSubterm(0);
        Type toReplace = termToReplace.getType();
        
        Term termToReplaceWith = application.getSubterm(1);
        Type replaceWith = termToReplaceWith.getType();
        
        Term termToReplaceIn = application.getSubterm(2);
        Term replaceTermAndType = application.getSubterm(3);
        
        Term replacement;
        if(Environment.getTrue().equals(replaceTermAndType)) {
            replacement = termToReplaceWith;
        } else if(Environment.getFalse().equals(replaceTermAndType)) {
            replacement = null;
        } else {
            throw new TermException("The 4th parameter of $$specialiseType needs to be either true or false, literally.");
        }
        
        if(!(toReplace instanceof TypeVariable)) {
            throw new TermException("Only type variables can be specialised, but not: " + toReplace);
        }
        
        TypeVariable typeVar = (TypeVariable) toReplace;
        
        Instantiator tr = new Instantiator();
        return tr.replace(typeVar, replaceWith, termToReplace, replacement, termToReplaceIn);
        
    }
    
    static class Instantiator extends RebuildingTermVisitor {
        
        private TypeVariable tyOfInterest;
        private Type instantiation;
        
        private Term termOfInterest;
        private Term termReplacement;
        
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
        protected void defaultVisitTerm(Term term) throws TermException {
            if(term.equals(termOfInterest)) {
                resultingTerm = termReplacement;
            } else {
                super.defaultVisitTerm(term);
            }
        };

        @Override
        protected Type modifyType(Type type) throws TermException {
            return type.accept(typeVariableReplacer, null);
        }
        
        public Term replace(TypeVariable typeVar, Type replaceWith, Term termToReplace,
                Term termToReplaceWith, Term termToReplaceIn) throws TermException {
        
            tyOfInterest = typeVar;
            instantiation = replaceWith;
            
            termOfInterest = termToReplace;
            termReplacement = termToReplaceWith;
            
            termToReplaceIn.visit(this);
            return resultingTerm == null ? termToReplaceIn : resultingTerm;
        }
        
    }
    

}
