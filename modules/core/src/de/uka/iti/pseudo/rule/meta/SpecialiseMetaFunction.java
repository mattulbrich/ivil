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

import static de.uka.iti.pseudo.term.TypeVariable.ALPHA;
import static de.uka.iti.pseudo.term.TypeVariable.BETA;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;

public class SpecialiseMetaFunction extends MetaFunction {

    private static final Type BOOL = Environment.getBoolType();

    public SpecialiseMetaFunction() throws EnvironmentException {
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
        
        if(replacement != null && !(termToReplace instanceof Variable)) {
            throw new TermException("Only variables can be substituted, but not: " + 
                    termToReplace + ": " + termToReplace.getClass());
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
        
        /*
         * count how often the type variable is bound in the current context
         */
        private int countBind = 0;
        
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
            if(countBind == 0 && term.equals(termOfInterest)) {
                resultingTerm = termReplacement;
            } else {
                super.defaultVisitTerm(term);
            }
        };
        
        @Override
        protected Type modifyType(Type type) throws TermException {
            return type.accept(typeVariableReplacer, null);
        }
        
        @Override
        protected void visitBindingVariable(Binding binding) throws TermException {
            binding.getVariable().visit(this);
        }

        /*
         * In case the type variable to be replaced is quantified over again,
         * the second binding to not to be instantiated.
         */
        @Override
        public void visit(TypeVariableBinding tyVarBinding) throws TermException {
            if(tyVarBinding.getBoundType().equals(tyOfInterest)) {
                resultingTerm = null;
            } else {
                super.visit(tyVarBinding);
            }
        }
        
        /*
         * In case that the variable to be replaced is quantified again,
         * the second binding must remain. However, the type MUST be instantiated,
         * otherwise wrong type variables may get exposed.
         *   (\T_all 'a; (\forall x as 'a; phi(x) & (\forall x as 'a; psi(x)))) |-
         *   
         * Here, an instantiation x => 0 is to not touch the second quantification,
         * BUT for the typing:
         *   phi(0) & (\forall x as int; psi(x))
         */
        public void visit(Binding binding) throws TermException {
            if(binding.getVariable().equals(termOfInterest)) {
                countBind ++;
                super.visit(binding);
                countBind --;
            } else {
                super.visit(binding);
            }
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
