package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import com.sun.istack.internal.Nullable;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
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
	
	private TypeVisitor instantiater = new DefaultTypeVisitor() {
        public Type visit(TypeVariable typeVariable) {
        	Type replace = instantiation.get(typeVariable);
        	if(replace != null)
        		return replace;
        	else 
        		return typeVariable;
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
	
	public Type leftUnify(Type adaptingType, Type fixType) throws UnificationException{
		Map<TypeVariable, Type> copy = new HashMap<TypeVariable, Type>(instantiation);
		
		try {
			leftUnify0(adaptingType, fixType);
			assert instantiate(adaptingType).equals(fixType);
			return fixType;
		} catch (UnificationException e) {
			// restore old mapping
			e.setDetailLocation("Cannot left-unify '" + adaptingType + "' and '" + fixType +"'");
			instantiation = copy;
			throw e;
		}

	}
	
	private void leftUnify0(Type adaptingType, Type fixType) throws UnificationException {
		
		if (adaptingType instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) adaptingType;
			if(instantiation.containsKey(tv))
				adaptingType = instantiate(tv);
		}
		
		if(adaptingType.equals(fixType))
			return;
		
		if (adaptingType instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) adaptingType;
			if(occursIn(tv, fixType))
				throw new UnificationException("Cannot unify (occur check)", tv, fixType);
			addMapping(tv, fixType);
			return;
		}
		
		assert adaptingType instanceof TypeApplication;
		
		if (!(fixType instanceof TypeApplication)) {
			throw new UnificationException("Cannot instantiate type variable on the right", adaptingType, fixType);
		}
		
		assert fixType instanceof TypeApplication;
		
		TypeApplication adaptApp = (TypeApplication) adaptingType;
		TypeApplication fixApp = (TypeApplication) fixType;
		
		if(adaptApp.getSort() != adaptApp.getSort()) {
			throw new UnificationException("Incompatible sorts", adaptApp, fixApp);
		}
		
		Type[] adaptArguments = adaptApp.getArguments();
		Type[] fixArguments = adaptApp.getArguments();
		
		for (int i = 0; i < fixArguments.length; i++) {
			// possibly wrap in try/catch to add detail information
			leftUnify0(adaptArguments[i], fixArguments[i]);
		}
	}
	
	public Type unify(Type type1, Type type2) throws UnificationException {
Map<TypeVariable, Type> copy = new HashMap<TypeVariable, Type>(instantiation);
		
		try {
			unify0(type1, type2);
			assert instantiate(type1).equals(instantiate(type2));
			return instantiate(type1);
		} catch (UnificationException e) {
			// restore old mapping
			e.setDetailLocation("Cannot left-unify '" + type1 + "' and '" + type2 +"'");
			instantiation = copy;
			throw e;
		}
	}

	
	

	private void unify0(Type type1, Type type2) throws UnificationException {
		
		if (type1 instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) type1;
			if(instantiation.containsKey(tv))
				type1 = instantiate(tv);
		}
		
		if (type2 instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) type2;
			if(instantiation.containsKey(tv))
				type2 = instantiate(tv);
		}
		
		if(type1.equals(type2))
			return;
		
		if (type1 instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) type1;
			if(occursIn(tv, type2))
				throw new UnificationException("Cannot unify (occur check)", type1, type2);
			addMapping(tv, type2);
			return;
		}
		
		if (type2 instanceof TypeVariable) {
			TypeVariable tv = (TypeVariable) type2;
			if(occursIn(tv, type1))
				throw new UnificationException("Cannot unify (occur check)", type1, type2);
			addMapping(tv, type1);
			return;
		}
		
		assert type1 instanceof TypeApplication;
		assert type2 instanceof TypeApplication;
		
		TypeApplication app1 = (TypeApplication) type1;
		TypeApplication app2 = (TypeApplication) type2;
		
		if(app1.getSort() != app2.getSort()) {
			throw new UnificationException("Incompatible sorts", app1, app2);
		}
		
		Type[] args1 = app1.getArguments();
		Type[] args2 = app2.getArguments();
		
		for (int i = 0; i < args1.length; i++) {
			// possibly wrap in try/catch to add detail information
			unify0(args1[i], args2[i]);
		}
		
	}

	private void addMapping(TypeVariable tv, Type type) {
		
		assert instantiation.get(tv) == null;
		assert !occursIn(tv, type);
		
		instantiation.put(tv, type);

// check whether this is needed or not		
//		for (TypeVariable t : instantiation.keySet()) {
//			instantiation.put(t, instantiate(instantiation.get(t)));
//		}
		
	}

	private boolean occursIn(final TypeVariable tv, Type type) {
		TypeVisitor vis = new DefaultTypeVisitor() {
			@Override
			public Type visit(TypeVariable typeVariable) throws TermException {
				if(typeVariable.equals(tv))
					throw new TermException("TypeVariable found!");
				return typeVariable;
			}
		};
		
		try {
			tv.visit(vis);
			// no exception: not found
			return false;
		} catch (TermException e) {
			// exception: type variable has been found
			return true;
		}
	}

	
	public Type instantiate(Type tv) {
		try {
			return tv.visit(instantiater);
		} catch (TermException e) {
			// not thrown in this code
			throw new Error(e);
		}
	}
}
