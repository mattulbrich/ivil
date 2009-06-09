package de.uka.iti.pseudo.term.creation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.term.ASTElement;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTModAssignment;
import de.uka.iti.pseudo.parser.term.ASTModIf;
import de.uka.iti.pseudo.parser.term.ASTModWhile;
import de.uka.iti.pseudo.parser.term.ASTModalityTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;

public class TypingResolver extends ASTDefaultVisitor {
    
    private Environment env;
    private Map<String, Type> boundVariables = new HashMap<String, Type>();
    private TypingContext typingContext = new TypingContext();
    private Type resultingType;
    
    public TypingResolver(Environment env) {
        super();
        this.env = env;
    }

    @Override
    protected void defaultVisit(ASTElement element) throws ASTVisitException {
        for(ASTElement e : element.getChildren()) {
            e.visit(this);
        }
    }
    
    @Override
    public void visit(ASTApplicationTerm applicationTerm)
            throws ASTVisitException {
        
        super.visit(applicationTerm);
        
        String functSymb = applicationTerm.getFunctionToken().image;
        Function funct = env.getFunction(functSymb);
        
        if(funct == null)
            throw new ASTVisitException("Unknown function symbol " + functSymb, applicationTerm);
        
        List<ASTTerm> subterms = applicationTerm.getSubterms();
        Type[] argumentTypes = funct.getArgumentTypes();
        Type resultType = funct.getResultType();
        
        if(argumentTypes.length != subterms.size())
            throw new ASTVisitException("Function symbol " + functSymb + " expects " + 
                    argumentTypes.length + " arguments, but received " + subterms.size(), applicationTerm);
        
        try {
			setTyping(applicationTerm, subterms, resultType, argumentTypes);
		} catch (UnificationException e) {
			throw new ASTVisitException("Type inference failed for function " + functSymb +
					"\nFunction: " + funct+
					"\n" + e.getDetailedMessage(), applicationTerm, e);
		}
    }
    
    @Override
    public void visit(ASTFixTerm fixTerm) throws ASTVisitException {
        
        super.visit(fixTerm);
        
        FixOperator fixOp = fixTerm.getFixOperator();
        Function funct = env.getFunction(fixOp.getName());
        
        if(funct == null)
            throw new ASTVisitException("Unknown fixed function symbol " + fixOp, fixTerm);
        
        List<ASTTerm> subterms = fixTerm.getSubterms();
        Type[] argumentTypes = funct.getArgumentTypes();
        Type resultType = funct.getResultType();
        
        if(argumentTypes.length != subterms.size())
            throw new ASTVisitException("Fixed function symbol " + fixOp + " expects " + 
                    argumentTypes.length + " arguments, but received " + subterms.size(), fixTerm);

        try {
            setTyping(fixTerm, subterms, resultType, argumentTypes);
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inference failed for function " + fixOp.getName() +
                    "\nFunction: " + funct+
                    "\n" + e.getDetailedMessage(), fixTerm);
        }
    }

    // DOC!
    @Override
    public void visit(ASTBinderTerm binderTerm)
            throws ASTVisitException {
        
        String var = binderTerm.getVariableToken().image;
        Type varType;
        ASTType astVarType = binderTerm.getVariableType();
        if(astVarType != null) {
            astVarType.visit(this);
            varType = resultingType;
        } else {
            varType = typingContext.newTypeVariable();
        }
            
        boundVariables.put(var, varType);
        super.visit(binderTerm);
        boundVariables.remove(var);
        
        String binderSymb = binderTerm.getBinderToken().image;
        Binder binder = env.getBinder(binderSymb);
        
        if(binder == null)
            throw new ASTVisitException("Unknown binder symbol " + binderSymb, binderTerm);
        
        List<ASTTerm> subterms = binderTerm.getSubterms();
        Type variableType = binder.getVarType();
        Type[] arguments = binder.getArgumentTypes();
        Type result = binder.getResultType();
        
        if(arguments.length != subterms.size())
            throw new ASTVisitException("Binder symbol " + binderSymb + " expects " + 
                    arguments.length + " arguments, but received " + subterms.size(), binderTerm);

        binderTerm.setVariableTyping(new Typing(varType, typingContext));

        try {
            typingContext.solveConstraint(variableType, varType);
            setTyping(binderTerm, subterms, result, arguments);
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inference failed for function " + binderSymb +
                    "\nFunction: " + binder +
                    "\n" + e.getDetailedMessage(), binderTerm);
        }
    }

    private void setTyping(ASTTerm term, List<ASTTerm> subterms, Type result, Type[] arguments) throws UnificationException {
        
        assert subterms.size() == arguments.length;
        
        Type[] sig = typingContext.makeNewSignature(result, arguments);
        
        term.setTyping(new Typing(sig[0], typingContext));
        
        for (int i = 1; i < sig.length; i++) {
            try {
				typingContext.solveConstraint(sig[i], subterms.get(i-1).getTyping().getRawtType());
			} catch (UnificationException e) {
				e.addDetail("in subterm " + (i-1));
				throw e;
			}
        }
    }
    
    @Override
    public void visit(ASTAsType asType) throws ASTVisitException {
        asType.getTerm().visit(this);
        
        asType.getAsType().visit(this);
        asType.setTyping(new Typing(resultingType, typingContext));
        
        try {
			typingContext.solveConstraint( 
			        asType.getTerm().getTyping().getRawtType(),
			        resultingType);
		} catch (UnificationException e) {
			throw new ASTVisitException("Type inference failed for explicitly typed term" +
					"\nExplicit Type: " + asType.getTyping().getRawtType() +
					"\n" + e.getDetailedMessage(), e);
			}
    }
    
    @Override
    public void visit(ASTIdentifierTerm identifierTerm)
            throws ASTVisitException {
        String name = identifierTerm.getSymbol().image;
        Function funcSymbol = env.getFunction(name);
        
        if(funcSymbol != null) {
            int arity = funcSymbol.getArgumentTypes().length;
            Type result = funcSymbol.getResultType();

            if(arity != 0)
                throw new ASTVisitException("Constant symbol " + funcSymbol + " expects " + 
                        arity + " arguments, but received none", identifierTerm);

            try {
				setTyping(identifierTerm, Collections.<ASTTerm>emptyList(), result, new Type[0]);
			} catch (UnificationException e) {
				throw new ASTVisitException("Type inference failed for constant " + name +
						"\nFunction: " + funcSymbol +
						"\n" + e.getDetailedMessage(), identifierTerm);
			}
        } else {
            Type tv = boundVariables.get(name);

            if(tv == null)
                throw new ASTVisitException("Unknown identifier: " + name, identifierTerm);

            identifierTerm.setTyping(new Typing(tv, typingContext));
        }

    }
    
    @Override
    public void visit(ASTSchemaVariableTerm schemaVariableTerm)
            throws ASTVisitException {
        String name = schemaVariableTerm.getToken().image;
        TypeVariable typeVar = new TypeVariable(name);
        schemaVariableTerm.setTyping(new Typing(typeVar, typingContext));
    }
    
    @Override
    public void visit(ASTNumberLiteralTerm numberLiteralTerm)
            throws ASTVisitException {
        
        numberLiteralTerm.setTyping(new Typing(env.getIntType(), typingContext));
        
    }
    
    @Override 
    public void visit(ASTListTerm listTerm) throws ASTVisitException {
        
        ASTTerm replacement = ShuntingYard.shuntingYard(env, listTerm);
        ASTElement parent = listTerm.getParent();
        
        parent.replaceChild(listTerm, replacement);
        
        replacement.visit(this);
    }
    
    
    @Override 
    public void visit(ASTModalityTerm modalityTerm)
            throws ASTVisitException {
        super.visit(modalityTerm);
        
        ASTTerm subterm = modalityTerm.getSubterms().get(0);
        modalityTerm.setTyping(subterm.getTyping());
        
    }
    
    @Override
    public void visit(ASTModAssignment modAssignment)
            throws ASTVisitException {
        
        super.visit(modAssignment);

        String symbol = modAssignment.getAssignedIdentifier().image;
        Function fct = env.getFunction(symbol);
        
        if(fct == null)
            throw new ASTVisitException("Unknown assigned identifier " + symbol, modAssignment);
        
        if(!fct.isAssignable())
            throw new ASTVisitException("Assigned identifier " + symbol + " is not assignable.", modAssignment);
        
        Type[] targetType = typingContext.makeNewSignature(fct.getResultType(), new Type[0]);
        
        try {
            typingContext.solveConstraint( 
                    targetType[0],
                    modAssignment.getAssignedTerm().getTyping().getRawtType());
        } catch (UnificationException e) {
            throw new ASTVisitException("Incompatibles types in assignmend." +
                    "\nIdentifier type: " + targetType[0] +
                    "\nAssigned term type: " + modAssignment.getAssignedTerm().getTyping().getRawtType() +
                    "\n" + e.getDetailedMessage(), e);
        }

    }

    @Override
    public void visit(ASTModIf modIf) throws ASTVisitException {
        
        super.visit(modIf);

        try {
            typingContext.solveConstraint(env.getBoolType(),
                    modIf.getConditionTerm().getTyping().getRawtType());
        } catch (UnificationException e) {
            throw new ASTVisitException("Condition in if-modality not boolean." +
                    "\ncondition term type: " +  modIf.getConditionTerm().getTyping().getRawtType() +
                    "\n" + e.getDetailedMessage(), e);
        }

        
    }

    @Override
    public void visit(ASTModWhile modWhile) throws ASTVisitException {
        super.visit(modWhile);

        try {
            typingContext.solveConstraint(env.getBoolType(),
                    modWhile.getConditionTerm().getTyping().getRawtType());
        } catch (UnificationException e) {
            throw new ASTVisitException("Condition in if-modality not boolean." +
                    "\ncondition term type: " +  modWhile.getConditionTerm().getTyping().getRawtType() +
                    "\n" + e.getDetailedMessage(), e);
        }
    }

    @Override
    public void visit(ASTTypeApplication typeRef) throws ASTVisitException {
        String typeName = typeRef.getTypeToken().image;
        
        List<ASTElement> children = typeRef.getChildren();
        Type[] args = new Type[children.size()];
        for (int i = 0; i < args.length; i++) {
            children.get(i).visit(this);
            args[i] = resultingType;
        }
        
        try {
            resultingType = env.mkType(typeName, args);
        } catch (TermException e) {
            throw new ASTVisitException(typeRef, e);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(typeRef, e);
        }
    }
    
    @Override
    public void visit(ASTTypeVar typeVar) throws ASTVisitException {
        resultingType = new TypeVariable(typeVar.getTypeVarToken().image.substring(1));
    }

}
