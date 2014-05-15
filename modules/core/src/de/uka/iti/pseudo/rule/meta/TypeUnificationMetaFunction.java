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
import de.uka.iti.pseudo.environment.AbstractMetaFunction;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.TypeVariableUnification;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;
import de.uka.iti.pseudo.util.RewindMap;

// TODO DOC
public class TypeUnificationMetaFunction extends AbstractMetaFunction {

    private static final Type BOOL = Environment.getBoolType();

    public TypeUnificationMetaFunction() throws EnvironmentException {
        //       type1, type2, replaceIn
        super(BOOL, "$$unifyTypes", ALPHA, BETA, BOOL);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {

        Term term1 = application.getSubterm(0);
        Type eqType1 = term1.getType();

        Term term2 = application.getSubterm(1);
        Type eqType2 = term2.getType();

        Term termToReplaceIn = application.getSubterm(2);

        return evaluate(eqType1, eqType2, termToReplaceIn);

    }

    public Term evaluate(Type eqType1, Type eqType2, Term termToReplaceIn) throws TermException {
        TypeVariableUnification tvu = new TypeVariableUnification();
        tvu.unify(eqType1, eqType2);
        tvu.instantiate(eqType2);

        Instantiator tr = new Instantiator();
        return tr.replace(tvu, termToReplaceIn);
    }

    static class Instantiator extends RebuildingTermVisitor {

        private TypeVariableUnification typeVariableUnification;

        @Override
        protected Type modifyType(Type type) throws TermException {
            return typeVariableUnification.instantiate(type);
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
            RewindMap<TypeVariable, Type> varMap = typeVariableUnification.getMap();
            int rewPos = varMap.getRewindPosition();
            varMap.remove(tyVarBinding.getBoundType());

            super.visit(tyVarBinding);

            varMap.rewindTo(rewPos);
        }

        public Term replace(TypeVariableUnification tvu, Term termToReplaceIn)
                throws TermException {
            this.typeVariableUnification = tvu;
            termToReplaceIn.visit(this);

            return resultingTerm == null ? termToReplaceIn : resultingTerm;
        }

    }


}
