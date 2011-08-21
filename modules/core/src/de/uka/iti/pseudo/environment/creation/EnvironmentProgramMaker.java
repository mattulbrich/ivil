/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProperties;
import de.uka.iti.pseudo.parser.file.ASTPropertiesDeclaration;
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.program.ASTLabelStatement;
import de.uka.iti.pseudo.parser.program.ASTSourceLineStatement;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.Util;

/**
 * This class is used to extract program asts from a file AST and to turn them
 * into program definitions.
 * 
 * The following steps are performed:
 * <ol>
 * <li>
 * </ol>
 * 
 * Use an instance of this class as visitor to a {@link ASTFile} element. It
 * will analyse all defined program ASTs and add programs to the environment
 * accordingly. 
 * 
 * This class provides means to resolve named labels into numeric ones. It keeps
 * track of the line numbers which were set.
 * 
 * The actual translation of a single {@link ASTStatement} into a
 * {@link Statement} is done via
 * {@link TermMaker#makeAndTypeStatement(ASTStatement, Environment)}.
 * 
 * @see EnvironmentMaker
 */
public class EnvironmentProgramMaker extends ASTDefaultVisitor {

    /**
     * The environment upon which we work and to which the programs are to be added.
     */
    private Environment env;
    
    /**
     * The raw statements are kept as a list of pairs of assigned source line numbers
     * and statement ast objects
     */
    private List<Pair<Integer,ASTStatement>> rawStatements =
        new ArrayList<Pair<Integer, ASTStatement>>();
    
    /**
     * The resulting list of statements.
     */
    private List<Statement> statements = new ArrayList<Statement>();
    
    /**
     * The resulting list of annotations.
     */
    private List<String> statementAnnotations = new ArrayList<String>();
    
    /**
     * The last set source line number.
     * They are set using "sourceline" pseudo statements.
     */
    private int lastSetSourceLineNumber;
    
    /**
     * A mapping from identifier labels to numeric labels.
     */
    private Map<String, Integer> labelMap = new HashMap<String, Integer>();
    
    /**
     * Instantiates a new environment program maker.
     * 
     * @param env
     *            the environment to work on
     */
    public EnvironmentProgramMaker(Environment env) {
        this.env = env;
    }

    /**
     * Resolve symbolic labels to numeric labels.
     * 
     * Works only on goto statements and checks for symbolic labels, looks them
     * up in {@link #labelMap} and replaces them by their numeric equivalent.
     * 
     * @throws ASTVisitException
     *             if an undefined label is referenced to
     */
    private void resolveLabels() throws ASTVisitException {
        for (Pair<Integer, ASTStatement> pair : rawStatements) {
            ASTStatement ast = pair.snd();
            if (ast instanceof ASTGotoStatement) {
                List<ASTTerm> targets = SelectList.select(ASTTerm.class, ast.getChildren());
                for (ASTTerm term : targets) {
                    if (term instanceof ASTIdentifierTerm) {
                        ASTIdentifierTerm id = (ASTIdentifierTerm) term;
                        String label = id.getSymbol().image;
                        Integer val = labelMap.get(label);
                        if(val == null)
                            throw new ASTVisitException("Unknown label in goto statement: " + label, id);
                        ast.replaceChild(term, new ASTNumberLiteralTerm(mkToken(val)));
                    }
                }
            }
        }
    }

    /**
     * Given an integer, create a parser token of type NATURAL
     * 
     * @param val
     *            the integer to make a token form, non-negative
     * 
     * @return the token of type NATURAL with the image set to the string
     *         induced by val
     */
    private Token mkToken(@NonNull Integer val) {
        assert val.intValue() >= 0;
        Token t = new Token();
        t.image = val.toString();
        t.kind = ParserConstants.NATURAL;
        return t;
    }

    /**
     * Turn the list of raw statements into a list of parsed statements.
     * 
     * @throws ASTVisitException
     *             the AST visit exception
     */
    private void makeStatements() throws ASTVisitException {
        for (Pair<Integer, ASTStatement> pair : rawStatements) {
            int sourcelinenumber = pair.fst();
            ASTStatement ast = pair.snd();
            Statement statement = TermMaker.makeAndTypeStatement(ast, sourcelinenumber, env);
            if(detectSchemaObject(statement))
                throw new ASTVisitException("Unallowed schema entity in statement", ast);
            statements.add(statement);
            
            // Annotations to statements are kept separately:
            String annotation = null;
            Token exp = ast.getTextAnnotation();
            if(exp != null)
                annotation = Util.stripQuotes(exp.image);
            statementAnnotations.add(annotation);
        }
    }
    
    /*
     * This is a depth visitor: descend to find program declarations for instance.
     */
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }
    
    /**
     * find schema variables in a statement.
     * @return true iff a subterm of statement contains a schema variable
     */
    private boolean detectSchemaObject(Statement statement) {
        for (Term subterm : statement.getSubterms()) {
            if(TermMatcher.containsSchemaObject(subterm))
                return true;
        }
        return false;
    }

    /**
     * by default, a statement is wrapped into a pair along with the current
     * source line number and stored in rawStatements.
     */
    protected void visitDefaultStatement(ASTStatement arg) throws ASTVisitException {
        rawStatements.add(Pair.make(lastSetSourceLineNumber, arg));
    }
    
    /**
     * source line statements change the currently set line number
     */
    public void visit(ASTSourceLineStatement arg) throws ASTVisitException {
        Token argument = arg.getLineNumberToken();
        
        // cannot fail because of Token type
        lastSetSourceLineNumber = Integer.parseInt(argument.image);
    }

    /**
     * map labels to their position in the program.
     * Complain if the label has already been defined. 
     */
    public void visit(ASTLabelStatement arg) throws ASTVisitException {
        String label = arg.getLabel().image;
        if(labelMap.containsKey(label)) {
            throw new ASTVisitException("The label " + label + " has already been defined earlier", arg);
        }
        labelMap.put(label, rawStatements.size());
    }
    
    /**
     * The entry point for a program declaration.
     * Clear all storing structures, visit children and store resulting program.
     */
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        
        rawStatements.clear();
        statements.clear();
        statementAnnotations.clear();
        lastSetSourceLineNumber = -1;
        labelMap.clear();
        
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
        
        resolveLabels();
        makeStatements();
        
        try {
            String name = arg.getName().image;
            
            Token sourceAST = arg.getSource();
            
            URL sourceResource = null;
            if (sourceAST != null) {
                String sourceFilename = Util.stripQuotes(sourceAST.image);
                URL res = new URL(env.getResourceName());
                // TODO support absolute file names as well? Like:
                // if(name.startsWith("/")) name = "file://" + name;
                sourceResource = new URL(res, sourceFilename);
            }
                
            Program program = new Program(name, sourceResource, statements, statementAnnotations, arg);
            env.addProgram(program);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        } catch (MalformedURLException e) {
            throw new ASTVisitException(arg, e);
        }
    }


    @Override
    public void visit(ASTProperties plugins) throws ASTVisitException {
    }

    @Override
    public void visit(ASTPropertiesDeclaration property) throws ASTVisitException {
    }

}
