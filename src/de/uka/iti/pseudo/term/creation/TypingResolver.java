package de.uka.iti.pseudo.term.creation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.term.ASTElement;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTTypeRef;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;

public class TypingResolver extends ASTDefaultVisitor {
    
    private Environment env;
    private Map<String, TypeVariable> boundVariables = new HashMap<String, TypeVariable>();
    private TypingContext typingContext;
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
					"\n" + e.getDetailedMessage(), applicationTerm);
		}
    }
    
    @Override
    public void visit(ASTBinderTerm binderTerm)
            throws ASTVisitException {
        
        String var = binderTerm.getVariableToken().image;
        boundVariables.put(var, typingContext.newTypeVariable());
        super.visit(binderTerm);
        boundVariables.remove(var);
        
        String binderSymb = binderTerm.getBinderToken().image;
        Binder binder = env.getBinder(binderSymb);
        
        if(binder == null)
            throw new ASTVisitException("Unknown binder symbol " + binderSymb, binderTerm);
        
        List<ASTTerm> subterms = binderTerm.getSubterms();
        Type[] arguments = binder.getArgumentTypes();
        Type result = binder.getResultType();
        
        if(arguments.length != subterms.size())
            throw new ASTVisitException("Binder symbol " + binderSymb + " expects " + 
                    arguments.length + " arguments, but received " + subterms.size(), binderTerm);
        
        try {
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
				e.setDetailLocation("subterm " + (i-1));
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
			typingContext.solveConstraint(resultingType, asType.getTyping().getRawtType());
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
            TypeVariable tv = boundVariables.get(name);

            if(tv == null)
                throw new ASTVisitException("Illegal free variable: " + name, identifierTerm);

            identifierTerm.setTyping(new Typing(tv, typingContext));
        }

    }
    
    @Override 
    public void visit(ASTListTerm listTerm) throws ASTVisitException {
        
        ASTTerm replacement = ShuntingYard.shuntingYard(env, listTerm);
        ASTElement parent = listTerm.getParent();
        
        parent.replaceChild(listTerm, replacement);
        
        replacement.visit(this);
    }
    
    
    @Override
    public void visit(ASTTypeRef typeRef) throws ASTVisitException {
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
            throw new ASTVisitException(e, typeRef);
        }
    }

}
