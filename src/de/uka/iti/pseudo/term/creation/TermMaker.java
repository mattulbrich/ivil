/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTModAssignment;
import de.uka.iti.pseudo.parser.term.ASTModCompound;
import de.uka.iti.pseudo.parser.term.ASTModIf;
import de.uka.iti.pseudo.parser.term.ASTModSkip;
import de.uka.iti.pseudo.parser.term.ASTModWhile;
import de.uka.iti.pseudo.parser.term.ASTModalityTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTTypeRef;
import de.uka.iti.pseudo.parser.term.ASTVisitor;
import de.uka.iti.pseudo.parser.term.ParseException;
import de.uka.iti.pseudo.parser.term.TermParser;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.Variable;

public class TermMaker implements ASTVisitor {

	private String content;
	private Environment env;
	private String fileName;
	private int beginLine;
	private int beginColumn;
	
	private Term endResultTerm;
	
	private Term resultTerm;
	private Type resultType;
	private Modality resultModality;

	public TermMaker(String content, Environment env, String fileName,
			int beginLine, int beginColumn) {
		this.content = content;
		this.env = env;
		this.fileName = fileName;
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
	}
	
	public TermMaker(String content, Environment env, String contextDescr) {
		this(content, env, contextDescr, 1, 1);
	}

	public Term getTerm() throws ParseException, ASTVisitException {
		
		if(endResultTerm == null) {
			TermParser parser = new TermParser(content, fileName, beginLine, beginColumn);
			ASTTerm ast = parser.parseTerm();
			ast.dumpTree();
			ast.visit(this);
			endResultTerm = resultTerm;
		}
		return endResultTerm;
	}
	
	private Term[] collectSubterms(ASTTerm term) throws ASTVisitException {
		List<ASTTerm> subterms = term.getSubterms();
		Term[] retval = new Term[subterms.size()];
		for (int i = 0; i < retval.length; i++) {
			subterms.get(i).visit(this);
			retval[i] = resultTerm;
		}
		return retval;
	}
	

	
	//
	// Visit methods
	//

	
	public void visit(ASTApplicationTerm applicationTerm)
			throws ASTVisitException {
		String functSymb = applicationTerm.getFunctionToken().image;
		Function funct = env.getFunction(functSymb);

		// checked elsewhere
		assert funct != null;
		
		Term[] subterms = collectSubterms(applicationTerm);
		
		Type type = applicationTerm.getTyping().getType();
		try {
			resultTerm = new Application(funct, type, subterms);
		} catch (TermException e) {
			throw new ASTVisitException(e, applicationTerm);
		}
	}

	
	public void visit(ASTBinderTerm binderTerm) throws ASTVisitException {
	    // TODO s. appliation
		String binderSymb = binderTerm.getBinderToken().image;
		Binder binder = env.getBinder(binderSymb);
		
		// checked elsewhere
        assert binder != null;
		
		binderTerm.getVariableType().visit(this);
		Type variableType = resultType;
		
		String variableName = binderTerm.getVariableToken().image;
		
		Term[] subterms = collectSubterms(binderTerm);
		
		try {
			resultTerm = new Binding(binder, binderTerm.getTyping().getType(), variableType, variableName, subterms);
		} catch(TermException ex) {
			throw new ASTVisitException(ex, binderTerm);
		}
	}
	
	
	public void visit(ASTFixTerm fixTerm) throws ASTVisitException {
		String fctName = fixTerm.getFixOperator().getName();
		Function function = env.getFunction(fctName);
		
		assert function != null;
		
		Term[] subterms = collectSubterms(fixTerm);
		
		Type type = fixTerm.getTyping().getType();
		try {
			resultTerm = new Application(function, type, subterms);
		} catch (TermException e) {
			throw new ASTVisitException(e, fixTerm);
			}
	}

	
	public void visit(ASTIdentifierTerm identifierTerm)
			throws ASTVisitException {
		String name = identifierTerm.getSymbol().image;
		Function funcSymbol = env.getFunction(name);
        Type type = identifierTerm.getTyping().getType();

        try {
			if (funcSymbol != null) {
			    resultTerm = new Application(funcSymbol, type);
			} else {
			    resultTerm = new Variable(name, type);
			}
		} catch (TermException e) {
			throw new ASTVisitException(e, identifierTerm);
		}
	}

	
	public void visit(ASTListTerm listTerm) throws ASTVisitException {
		
	    throw new Error("This must not appear. These terms must have been resolved earlier");
		
	}
	
	public void visit(ASTModalityTerm modalityTerm) throws ASTVisitException {
		
		modalityTerm.getModality().visit(this);
		Modality modality = resultModality;
		
		Term[] subterms = collectSubterms(modalityTerm);
		assert subterms.length == 1;
		
		resultTerm = new ModalityTerm(modality, subterms[0]);
	}

	
	public void visit(ASTModAssignment modAssignment) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	
	public void visit(ASTModCompound modCompound) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	
	public void visit(ASTModIf modIf) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	
	public void visit(ASTModSkip modSkip) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	
	public void visit(ASTModWhile modWhile) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	
	public void visit(ASTNumberLiteralTerm numberLiteralTerm)
			throws ASTVisitException {
		Function funct = env.getNumberLiteral(numberLiteralTerm.getNumberToken().image);
		try {
            resultTerm = new Application(funct, new TypeApplication(env.getSortInt()));
		} catch (TermException e) {
		    throw new ASTVisitException(e, numberLiteralTerm);
        }
	}

	
	public void visit(ASTTypeRef typeRef) throws ASTVisitException {
		List<ASTTypeRef> subty = typeRef.getArgumentTypeRefs();
		Type[] retval = new Type[subty.size()];
		for (int i = 0; i < retval.length; i++) {
			subty.get(i).visit(this);
			retval[i] = resultType;
		}
			
		try {
			resultType = env.mkType(typeRef.getTypeToken().image, retval);
		} catch (TermException e) {
			throw new ASTVisitException(e, typeRef);
		}
	}

	
	public void visit(ASTOperatorIdentifierTerm operatorIdentifierTerm)
			throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

    public void visit(ASTAsType asType) throws ASTVisitException {
        // DOC
        // TODO Auto-generated method stub
        
    }

	

}
