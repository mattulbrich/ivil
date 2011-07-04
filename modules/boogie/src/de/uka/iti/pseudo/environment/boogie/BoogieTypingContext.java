package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.term.creation.TypingContext;

// FIXME is this class still used? if not delete it and forget about it
class BoogieTypingContext extends TypingContext {
    // a visitor which is universally used to replace type variables by other
    // types
    private static final TypeVariableReplacer replacer = new TypeVariableReplacer();

    private final Environment env;

    public BoogieTypingContext(Environment env) {
        unifier = new BoogieUnifier();
        this.env = env;
    }

    private static class TypeVariableReplacer extends RebuildingTypeVisitor<Map<TypeVariable, Type>> {
        @Override
        public Type visit(TypeVariable typeVariable, Map<TypeVariable, Type> map) throws TermException {
            if (map.containsKey(typeVariable))
                return map.get(typeVariable);
            return typeVariable;
        }
    }

    public class BoogieUnifier extends TypeUnification.Unifier {

        @Override
        public Void visit(TypeApplication adaptApp, Type fixType) throws TermException {
            if (fixType instanceof SchemaType) {
                fixType.accept(this, adaptApp);
            } else
            
            if(fixType instanceof TypeApplication) {
                TypeApplication fixApp = (TypeApplication) fixType;
                
                // TODO if infering boogie map types we have to unpack maps
                // here, as the equality rules allow for definitions like
    
                // Rel T U = [T,U]bool
                // x:[int,int]bool
    
                // in this case, x would as well have the type rel(T,U) but it
                // is impossible to know that?
                
                if (adaptApp.getSort() != fixApp.getSort()) {
                    // if both sorts represent map aliases, the maps can be
                    // considered equal, if their load function can be unified
                    // by ignoring the map field and replacing at most variables
                    // bound by the map type
                    
                    // if we got two maps, try to unify domain and range instead
                    Function loadA, loadF;
                    loadA = env.getFunction("$load_" + adaptApp.getSort().getName());
                    loadF = env.getFunction("$load_" + fixApp.getSort().getName());

                    if (null != loadA && null != loadF) {
                        // we have to replace the instantiations of this map type
                        if(loadA.getArity()!=loadF.getArity())
                            throw new UnificationException("Incompatible sorts",
                                    adaptApp, fixApp); 

                        // build map signatures
                        Type[] sigA = loadA.getArgumentTypes().clone();
                        Type[] sigF = loadF.getArgumentTypes().clone();

                        // reuse the map argument field as result field
                        sigA[0] = loadA.getResultType();
                        sigF[0] = loadF.getResultType();


                        // adapt map signatures to the actually used types
                        {
                            Map<TypeVariable, Type> map = new HashMap<TypeVariable, Type>();
                            
                            List<Type> from, to;
                            from = ((TypeApplication) loadA.getArgumentTypes()[0]).getArguments();
                            to = adaptApp.getArguments();
                            for(int i = 0; i < adaptApp.getArguments().size(); i++)
                                map.put((TypeVariable) from.get(i), to.get(i));
                            
                            for (int i = 0; i < sigA.length; i++)
                                sigA[i] = sigA[i].accept(replacer, map);
                        }

                        {
                            Map<TypeVariable, Type> map = new HashMap<TypeVariable, Type>();

                            List<Type> from, to;
                            from = ((TypeApplication) loadF.getArgumentTypes()[0]).getArguments();
                            to = fixApp.getArguments();
                            for (int i = 0; i < fixApp.getArguments().size(); i++)
                                map.put((TypeVariable) from.get(i), to.get(i));

                            for (int i = 0; i < sigF.length; i++)
                                sigF[i] = sigF[i].accept(replacer, map);
                        }

                        // unify signatures
                        for (int i = 0; i < sigA.length; i++)
                            sigA[i].accept(this, sigF[i]);

                        return null;
                    }
    
                    throw new UnificationException("Incompatible sorts",
                            adaptApp, fixApp);
                }
    
                List<Type> adaptArguments = adaptApp.getArguments();
                List<Type> fixArguments = fixApp.getArguments();
    
                for (int i = 0; i < fixArguments.size(); i++) {
                    // possibly wrap in try/catch to add detail information
                    adaptArguments.get(i).accept(this, fixArguments.get(i));
                }
            } else {
                
                throw new UnificationException("Cannot unify (by class)", adaptApp, fixType);
            }
            return null;
        }
    }
}
