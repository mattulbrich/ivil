/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTRawTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTType;
import de.uka.iti.pseudo.parser.file.ASTTypeRef;
import de.uka.iti.pseudo.parser.file.ASTTypeVar;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.parser.file.Token;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.SelectList;

public class EnvironmentMaker extends ASTFileDefaultVisitor {

    // TODO WHERE MUST THIS CONSTANT LIVE
    private static File SYS_DIR = new File("sys");

    private Environment env;

    private Type resultingTypeRef;
    
    private FileParser parser;

	private Term resultingTerm;

	private File file;

	private Term problemTerm;

    public EnvironmentMaker(FileParser parser, File file) {
        this.parser = parser;
        this.file = file;
    }
    
    public Environment getEnvironment() throws FileNotFoundException, ParseException, ASTVisitException {
    	if(env == null) {
    		env = new Environment();
    		ASTFile f = parser.parseFile(file);
    		visit(f);
    	}
        return env;
    }

    private String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    private File mkFile(String toplevel, String filename) {
        File ret;
        if (filename.charAt(0) == '$') {
            ret = new File(SYS_DIR, filename.substring(1));
        } else {
            ret = new File(new File(toplevel).getParentFile(), filename);
        }
        return ret;
    }

    protected void visitDefault(ASTFileElement arg) throws ASTVisitException {
        for (ASTFileElement child : arg) {
            child.visit(this);
        }
    }

    public void visit(ASTFile arg) throws ASTVisitException {

        List<ASTDeclarationBlock> blocks = arg.getDeclarationBlocks();

        for (ASTIncludeDeclarationBlock include : SelectList.select(
                ASTIncludeDeclarationBlock.class, blocks)) {
            include.visit(this);
        }

        for (ASTSortDeclarationBlock sortDecl : SelectList.select(
                ASTSortDeclarationBlock.class, blocks)) {
            sortDecl.visit(this);
        }

        for (ASTFunctionDeclarationBlock funDecl : SelectList.select(
                ASTFunctionDeclarationBlock.class, blocks)) {
            funDecl.visit(this);
        }

        for (ASTBinderDeclarationBlock bindDecl : SelectList.select(
                ASTBinderDeclarationBlock.class, blocks)) {
            bindDecl.visit(this);
        }

        for (ASTRule rule : SelectList.select(ASTRule.class, blocks)) {
            rule.visit(this);
        }
        
        ASTRawTerm problem = arg.getProblemTerm();
        if(problem != null) {
        	problem.visit(this);
        	problemTerm = resultingTerm;
        }

    }

    public void visit(ASTIncludeDeclarationBlock arg) throws ASTVisitException {

        for (Token token : arg.getIncludeStrings()) {
            String filename = stripQuotes(token.image);
            File file = mkFile(arg.getFileName(), filename);
            try {
                visit(parser.parseFile(file));
            } catch (FileNotFoundException e) {
                throw new ASTVisitException("Cannot include " + file
                        + " (not found)", arg);
            } catch (ParseException e) {
                throw new ASTVisitException("Error while including file "
                        + file, e);
            }
        }

    }

    public void visit(ASTSortDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        int arity = arg.getTypeVariables().size();

        env.addSort(new Sort(name, arity, arg));
    }

    public void visit(ASTFunctionDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;

        arg.getRangeType().visit(this);
        Type resultTy = resultingTypeRef;
        List<ASTType> argumentTypes = arg.getArgumentTypes();
        Type argTy[] = new Type[argumentTypes.size()];
        int arity = argTy.length;

        for (int i = 0; i < arity; i++) {
            argumentTypes.get(i).visit(this);
            argTy[i] = resultingTypeRef;
        }

        env.addFunction(new Function(name, resultTy, argTy, arg.isUnique(), arg));

        if (arg.isInfix()) {
            if(arity != 2) 
            	throw new ASTVisitException("Arity of infix operator " + name + " is not 2", arg);
            
            String infix = arg.getOperatorIdentifier().image;
            int precedence = Integer.parseInt(arg.getPrecedence().image);
            env.addInfixOperator(new FixOperator(name, infix, precedence, 2, arg));
        }
        
        if(arg.isPrefix()) {
        	if(arity != 1) 
            	throw new ASTVisitException("Arity of prefix operator " + name + " is not 1", arg);
        	
        	String prefix = arg.getOperatorIdentifier().image;        	
        	int precedence = Integer.parseInt(arg.getPrecedence().image);
            env.addPrefixOperator(new FixOperator(name, prefix, precedence, 1, arg));
        }
    }

    public void visit(ASTBinderDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;

        arg.getRangeType().visit(this);
        Type rangeTy = resultingTypeRef;

        arg.getVariableType().visit(this);
        Type varTy = resultingTypeRef;

        List<ASTType> argumentTypes = arg.getTypeReferenceList();
        Type domTy[] = new Type[argumentTypes.size()];

        for (int i = 0; i < domTy.length; i++) {
            argumentTypes.get(i).visit(this);
            domTy[i] = resultingTypeRef;
        }

        env.addBinder(new Binder(name, rangeTy, varTy, domTy, arg));
    }
    
    public void visit(ASTTypeRef arg) throws ASTVisitException {
        
        String name = arg.getTypeToken().image;
        
        List<ASTType> argumentTypes = arg.getArgTypes();
        Type domTy[] = new Type[argumentTypes.size()];

        for (int i = 0; i < domTy.length; i++) {
            argumentTypes.get(i).visit(this);
            domTy[i] = resultingTypeRef;
        }
        
        try {
            resultingTypeRef = env.mkType(name, domTy);
        } catch (Exception e) {
            throw new ASTVisitException(e, arg);
        }
    }
    
    public void visit(ASTTypeVar arg) throws ASTVisitException {
        resultingTypeRef = new TypeVariable(arg.getTypeVarToken().image.substring(1));
    }

    public void visit(ASTRawTerm arg) throws ASTVisitException {
    	Token token = arg.getTermToken();
    	String content = stripQuotes(token.image);
    	try {
			resultingTerm = TermMaker.makeTerm(content, env, arg.getFileName(), token.beginLine, token.beginColumn);
		} catch (de.uka.iti.pseudo.parser.term.ParseException e) {
			throw new ASTVisitException(e, arg);
		}
    }

	public Term getProblemTerm() {
		return problemTerm;
	}

}
