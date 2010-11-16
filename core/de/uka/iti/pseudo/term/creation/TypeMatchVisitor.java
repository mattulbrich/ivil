package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;

public class TypeMatchVisitor extends DefaultTypeVisitor<Type> {
    
    private TermMatcher termMatcher;
    
    public TypeMatchVisitor(TermMatcher termMatcher) {
        super();
        this.termMatcher = termMatcher;
    }

    @Override
    public Void visit(SchemaType schemaTypeVariable, Type argument)
            throws TermException {
        
        String varName = schemaTypeVariable.getVariableName();
        tryInstantiation(varName, argument);
        return null;
    }
    
    public void tryInstantiation(String varName, Type argument) throws UnificationException {
        
        Type thisSigma = termMatcher.getTypeFor(varName);
        if(thisSigma == null) {
            termMatcher.addTypeInstantiation(varName, argument);
        } else {
            if (!thisSigma.equals(argument)) {
                throw new UnificationException("Incomparable types", thisSigma, argument);
            }
        }
    }
    
    @Override
    public Void visit(TypeApplication typeApp, Type argument)
            throws TermException {
        Sort sort = typeApp.getSort();
        
        if (argument instanceof TypeApplication) {
            TypeApplication otherApp = (TypeApplication) argument;
            if(sort != otherApp.getSort()) {
                throw new UnificationException("Incomparable sorts", typeApp, argument);
            }
            
            Type[] args = typeApp.getArguments();
            Type[] otherArgs = otherApp.getArguments();
            for(int i = 0; i < sort.getArity(); i++) {
                args[i].accept(this, otherArgs[i]);
            }
            
        } else {
            throw new UnificationException("Incomparable types", typeApp, argument);
        }
        return null;
    }
    
    @Override
    public Void visit(TypeVariable typeVar, Type argument)
            throws TermException {
        
        if(!typeVar.equals(argument)) {
            throw new UnificationException("Incomparable types (type var)", typeVar, argument);
        }

        return null;
    }
    
}
