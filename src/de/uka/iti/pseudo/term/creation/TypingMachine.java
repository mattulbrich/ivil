package de.uka.iti.pseudo.term.creation;

import java.util.Stack;

import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.util.Pair;

import nonnull.NonNull;

@NonNull
public class TypingMachine implements TermVisitor {
	
	private BoundVariableStack boundVariableStack = new BoundVariableStack();
	
	private Type resultingType;
	
	public TypingMachine() {
		
	}
	
	private Type[] visitSubterms(Term term) {
		Type retval[] = new Type[term.countSubterms()];
		for (int i = 0; i < retval.length; i++) {
			term.getSubterm(i).visit(this);
			retval[i] = resultingType;
		}
		return retval;
	}

	

	@Override
	public void visit(Variable variable) {
		Type ty = boundVariableStack.find(variable.getName());
		if(ty == null) {
			
		}
	}

	@Override
	public void visit(ModalityTerm modalityTerm) {
		visitSubterms(modalityTerm);
	}



	@Override
	public void visit(Binding binding) {
		String var = binding.getVariableName();
		Type varTy = binding.getVariableType();
		boundVariableStack.push(var, varTy);
		Type[] kidTy = visitSubterms(binding);
		boundVariableStack.drop();
	}

	@Override
	public void visit(Application application) {
		

	}

}
