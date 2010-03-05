/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
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
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.program.ASTAssertStatement;
import de.uka.iti.pseudo.parser.program.ASTAssignmentStatement;
import de.uka.iti.pseudo.parser.program.ASTAssumeStatement;
import de.uka.iti.pseudo.parser.program.ASTEndStatement;
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaUpdateTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
import de.uka.iti.pseudo.parser.term.ASTUpdateTerm;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;


// TODO DOC
public class TypingResolver extends ASTDefaultVisitor {
    
    private Environment env;
    private Map<String, Type> boundVariablesTypes = new HashMap<String, Type>();
    private TypingContext typingContext;
    private Type resultingType;
    
    public TypingResolver(Environment env, TypingContext typingContext) {
        super();
        this.env = env;
        this.typingContext = typingContext;
    }
    
    public TypingContext getTypingContext() {
        return typingContext;
    }

    @Override
    protected void visitDefault(ASTElement element) throws ASTVisitException {
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
    
    private void setTyping(ASTTerm term, List<ASTTerm> subterms, Type result, Type[] arguments) throws UnificationException {
        
        assert subterms.size() == arguments.length;
        
        Type[] sig = typingContext.makeNewSignature(result, arguments);
        
        term.setTyping(new Typing(sig[0], typingContext));
        
        for (int i = 1; i < sig.length; i++) {
            try {
                typingContext.solveConstraint(sig[i], subterms.get(i-1).getTyping().getRawType());
            } catch (UnificationException e) {
                e.addDetail("in subterm " + (i-1));
                throw e;
            }
        }
    }


    // DOC!
    @Override
    public void visit(ASTBinderTerm binderTerm)
            throws ASTVisitException {
        
        String var = binderTerm.getVariableToken().image;
        Type varType = null;
        ASTType astVarType = binderTerm.getVariableType();
        
        if(!var.startsWith("%")) {
            varType = typingContext.newTypeVariable();
            boundVariablesTypes.put(var, varType);
            super.visit(binderTerm);
            boundVariablesTypes.remove(var);
        } else {
            varType = new TypeVariable(var);
            super.visit(binderTerm);
        }
        
        //
        // handle explicit (\binder x as type; ...) typings
        if(astVarType != null) {
            astVarType.visit(this);
            try {
                typingContext.solveConstraint(varType, resultingType);
            } catch (UnificationException e) {
                throw new ASTVisitException("Type inference failed for binder variable for " + var +
                        "\nVariable type: " + varType +
                        "\n" + e.getDetailedMessage(), binderTerm, e);
            }
        }
        
        binderTerm.setVariableTyping(new Typing(varType, typingContext));
        
        String binderSymb = binderTerm.getBinderToken().image;
        Binder binder = env.getBinder(binderSymb);
        
        if(binder == null)
            throw new ASTVisitException("Unknown binder symbol " + binderSymb, binderTerm);
        
        List<ASTTerm> subterms = binderTerm.getSubterms();
        Type[] arguments = binder.getArgumentTypes();
        
        if(arguments.length != subterms.size())
            throw new ASTVisitException("Binder symbol " + binderSymb + " expects " + 
                    arguments.length + " arguments, but received " + subterms.size(), binderTerm);

        try {
            setBinderTyping(binderTerm, subterms, binder);
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inference failed for binder " + binderSymb +
                    "\nBinder: " + binder +
                    "\n" + e.getDetailedMessage(), binderTerm, e);
        }
    }
    
    // special version of setTyping adapted for the needs of binding terms
    // TODO DOC this method
    private void setBinderTyping(ASTBinderTerm term, List<ASTTerm> subterms, Binder binder) throws UnificationException {
        
        Type resulType = binder.getResultType();
        Type varType = binder.getVarType();
        Type[] argType = binder.getArgumentTypes();
     
        Type[] sig = typingContext.makeNewSignature(resulType, varType, argType);
        
        term.setTyping(new Typing(sig[0], typingContext));

        typingContext.solveConstraint(sig[1], term.getVariableTyping().getRawType());
        
        for (int i = 2; i < sig.length; i++) {
            try {
                typingContext.solveConstraint(sig[i], subterms.get(i-2).getTyping().getRawType());
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
			        asType.getTerm().getTyping().getRawType(),
			        resultingType);
		} catch (UnificationException e) {
			throw new ASTVisitException("Type inference failed for explicitly typed term" +
					"\nExplicit Type: " + asType.getTyping().getRawType() +
					"\n" + e.getDetailedMessage(), e);
			}
    }
    
    @Override
    public void visit(ASTIdentifierTerm identifierTerm)
            throws ASTVisitException {
        String name = identifierTerm.getSymbol().image;
        Function funcSymbol = env.getFunction(name);
        Type tv = boundVariablesTypes.get(name);
        
        if(tv != null) {
        	identifierTerm.setTyping(new Typing(tv, typingContext));
        } else if(funcSymbol != null) {
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
        	throw new ASTVisitException("Unknown identifier: " + name, identifierTerm);
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
    public void visit(ASTUpdateTerm updateTerm) throws ASTVisitException {
        super.visit(updateTerm);
        ASTTerm subterm = updateTerm.getSubterms().get(0);
        updateTerm.setTyping(subterm.getTyping());
    }
    
    @Override 
    public void visit(ASTSchemaUpdateTerm schemaUpdateTerm) throws ASTVisitException {
        super.visit(schemaUpdateTerm);
        ASTTerm subterm = schemaUpdateTerm.getSubterms().get(0);
        schemaUpdateTerm.setTyping(subterm.getTyping());
    } 


    
    @Override
    public void visit(ASTNumberLiteralTerm numberLiteralTerm)
            throws ASTVisitException {
        
        numberLiteralTerm.setTyping(new Typing(Environment.getIntType(), typingContext));
        
    }
    
    @Override 
    public void visit(ASTListTerm listTerm) throws ASTVisitException {
        
        ASTTerm replacement = ShuntingYard.shuntingYard(env, listTerm);
        ASTElement parent = listTerm.getParent();
        
        parent.replaceChild(listTerm, replacement);
        
        replacement.visit(this);
    }
    
    
    @Override 
    public void visit(ASTProgramTerm programTerm) throws ASTVisitException {
        super.visit(programTerm);
        
        programTerm.setTyping(new Typing(Environment.getBoolType(), typingContext));
        
        if(programTerm.isSchema()) {
            TypeVariable tv = new TypeVariable(programTerm.getLabel().image);
            try {
                typingContext.solveConstraint(Environment.getBoolType(), tv);
            } catch (UnificationException e) {
                throw new ASTVisitException("The schema variable inside schematic program term must be to boolean", programTerm, e);
            } 
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
    
    @Override 
    public void visit(ASTAssertStatement arg) throws ASTVisitException {
        super.visit(arg);
        try {
            typingContext.solveConstraint(Environment.getBoolType(), arg.getTerm().getTyping().getRawType());
        } catch (UnificationException e) {
            throw new ASTVisitException("An assert statement needs a boolean argument", arg, e);
        }
    }
    
    @Override 
    public void visit(ASTAssumeStatement arg) throws ASTVisitException {
        super.visit(arg);
        try {
            typingContext.solveConstraint(Environment.getBoolType(), arg.getTerm().getTyping().getRawType());
        } catch (UnificationException e) {
            throw new ASTVisitException("An assume statement needs a boolean argument", arg, e);
        }
    }
    
    @Override 
    public void visit(ASTEndStatement arg) throws ASTVisitException {
        super.visit(arg);
        try {
            typingContext.solveConstraint(Environment.getBoolType(), arg.getTerm().getTyping().getRawType());
        } catch (UnificationException e) {
            throw new ASTVisitException("An end statement needs a boolean argument", arg, e);
        }
    }
    
    @Override 
    public void visit(ASTGotoStatement arg) throws ASTVisitException {
        // do not type resolve labeled goto statements: 
        // labels are *not* integer variables, and therefore unknown to the environment
        // however, schema variables may appear here, they need to be typed
        for(ASTElement child : arg.getChildren()) {
            if(child instanceof ASTSchemaVariableTerm) {
                ASTSchemaVariableTerm sv = (ASTSchemaVariableTerm)child;
                child.visit(this);
                try {
                    typingContext.solveConstraint(Environment.getIntType(), sv.getTyping().getRawType());
                } catch (UnificationException e) {
                    throw new ASTVisitException("An end statement needs a boolean argument", arg, e);
                }
            }
        }
    }
    
    @Override 
    public void visit(ASTAssignmentStatement arg) throws ASTVisitException {
        super.visit(arg);
        try {
            typingContext.solveConstraint(arg.getTarget().getTyping().getRawType(), 
                    arg.getTerm().getTyping().getRawType());
        } catch (UnificationException e) {
            throw new ASTVisitException("Cannot infer types in an assignment", arg, e);
        }
    }

}
