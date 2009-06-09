/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.List;
import java.util.Stack;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
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
		String functSymb = applicationTerm.getFunctionSymbol().image;
		Function funct = env.getFunction(functSymb);
		
		if(funct == null)
			throw new ASTVisitException("Unknown function symbol " + functSymb, applicationTerm);
		
		Term[] subterms = collectSubterms(applicationTerm);
		
		try {
			resultTerm = new Application(funct, subterms);
		} catch(TermException ex) {
			throw new ASTVisitException(ex, applicationTerm);
		}
	}

	
	public void visit(ASTBinderTerm binderTerm) throws ASTVisitException {
		String binderSymb = binderTerm.getBinderToken().image;
		Binder binder = env.getBinder(binderSymb);
		
		if(binder == null)
			throw new ASTVisitException("Unknown binder symbol " + binderSymb, binderTerm);
		
		binderTerm.getVariableType().visit(this);
		Type variableType = resultType;
		
		String variableName = binderTerm.getVariableToken().image;
		
		Term[] subterms = collectSubterms(binderTerm);
		
		try {
			resultTerm = new Binding(binder, variableType, variableName, subterms);
		} catch(TermException ex) {
			throw new ASTVisitException(ex, binderTerm);
		}
	}
	
	
	public void visit(ASTFixTerm fixTerm) throws ASTVisitException {
		String fctName = fixTerm.getFixOperator().getName();
		Function function = env.getFunction(fctName);
		
		if(function == null) {
			// cannot happen, but save is save.
			throw new ASTVisitException("Unknown function symbol " + fctName, fixTerm);
		}
		
		Term[] subterms = collectSubterms(fixTerm);
		
		try {
			resultTerm = new Application(function, subterms);
		} catch(TermException ex) {
			throw new ASTVisitException(ex, fixTerm);
		}
		
	}

	
	public void visit(ASTIdentifierTerm identifierTerm)
			throws ASTVisitException {
		String name = identifierTerm.getSymbol().image;
		Function funcSymbol = env.getFunction(name);
		try {
			if(funcSymbol != null) {
				resultTerm = new Application(funcSymbol);
			} else {
				resultTerm = new Variable(name);
			}
		} catch(TermException ex) {
			throw new ASTVisitException(ex, identifierTerm);
		}
	}

	
	public void visit(ASTListTerm listTerm) throws ASTVisitException {
		
		ASTTerm replacement = ShuntingYard.shuntingYard(env, listTerm);
		replacement.visit(this);
		
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
			resultTerm = new Application(funct);
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

	

}
