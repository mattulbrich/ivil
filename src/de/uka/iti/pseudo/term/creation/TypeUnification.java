package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;

@NonNull
public class TypeUnification {
	
	private final static TypeVisitor VARIANT_VISITOR = new DefaultTypeVisitor() {
		public Type visit(TypeVariable typeVariable) {
			return new TypeVariable("#" + typeVariable.getVariableName());
		};
	};

	public static Type makeVariant(Type type) {
		try {
			return type.visit(VARIANT_VISITOR);
		} catch (TermException e) {
			// never thrown in this code
			throw new Error(e);
		}
	}
	
	private Map<TypeVariable, Type> instantiation = new HashMap<TypeVariable, Type>();
	
	public Type leftUnify(Type adaptingType, Type fixType)  throws UnificationException{
		throw new NoSuchMethodError();
	}
	
	public Type unify(Type type1, Type type2) throws UnificationException {
		throw new NoSuchMethodError();
	}
	
	public Map<TypeVariable, Type> getInstantiation() {
		return instantiation;
	}
}
