package de.uka.iti.pseudo.term;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
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

	@Override
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

	@Override
	public void visit(ASTBinderTerm binderTerm) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTIdentifierTerm identifierTerm)
			throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTListTerm listTerm) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTModalityTerm modalityTerm) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTModAssignment modAssignment) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTModCompound modCompound) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTModIf modIf) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTModSkip modSkip) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTModWhile modWhile) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTNumberLiteralTerm numberLiteralTerm)
			throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTTypeRef typeRef) throws ASTVisitException {
		// TODO Auto-generated method stub
		
	}

}
