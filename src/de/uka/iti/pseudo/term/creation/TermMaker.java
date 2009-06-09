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

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTHeadElement;
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
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
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
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

// TODO: Auto-generated Javadoc
/**
 * This class has two purposes:
 * 
 * It holds the {@link #makeTerm(String, Environment)} methods (and similar)
 * that allow to create types from strings.
 * 
 * It is a AST Term visitor that creates a term out of a ASTTerm object.
 */
public class TermMaker implements ASTVisitor {

    //
    // --- Static translation nature
    //

    /**
     * Make a term from a string.
     * 
     * A parser is created, a term is parsed the AST is then subjected to a
     * {@link TypingResolver} visitor that infers the necessary typing
     * information. An instance of this class then creates a {@link Term} object
     * out of the AST.
     * 
     * @param content
     *            the string to parse
     * @param env
     *            the environment
     * @param fileName
     *            the file name to report to the parser
     * @param beginLine
     *            the begin line to report to the parser
     * @param beginColumn
     *            the begin column to report to the parser
     * 
     * @return a term representing the string
     * 
     * @throws ParseException
     *             thrown by the parser
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static @NonNull Term makeTerm(@NonNull String content,
            @NonNull Environment env, @NonNull String fileName, int beginLine,
            int beginColumn) throws ParseException, ASTVisitException {

        TermParser parser = new TermParser(content, fileName, beginLine,
                beginColumn);
        ASTTerm ast = parser.parseTerm();

        // ast.dumpTree();

        // We have to embed the AST into a container because the structure may
        // change if it is a ASTListTerm.
        ASTHeadElement head = new ASTHeadElement(ast);
        TypingResolver typingResolver = new TypingResolver(env);
        ast.visit(typingResolver);
        ast = (ASTTerm) head.getWrappedElement();

        // ast.dumpTree();

        TermMaker termMaker = new TermMaker(env);
        ast.visit(termMaker);

        return termMaker.resultTerm;
    }

    /**
     * Make a term from a string.
     * 
     * A parser is created, a term is parsed the AST is then subjected to a
     * {@link TypingResolver} visitor that infers the necessary typing
     * information. An instance of this class then creates a {@link Term} object
     * out of the AST.
     * 
     * @param content
     *            the string to parse
     * @param env
     *            the environment
     * 
     * @return a term representing the string
     * 
     * @throws ParseException
     *             thrown by the parser
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static @NonNull Term makeTerm(@NonNull String content,
            @NonNull Environment env) throws ParseException, ASTVisitException {
        return makeTerm(content, env, "");
    }

    /**
     * Make a term from a string.
     * 
     * A parser is created, a term is parsed the AST is then subjected to a
     * {@link TypingResolver} visitor that infers the necessary typing
     * information. An instance of this class then creates a {@link Term} object
     * out of the AST.
     * 
     * @param content
     *            the string to parse
     * @param env
     *            the environment
     * @param context
     *            the context name to be reported as file name to the parser
     * 
     * @return a term representing the string
     * 
     * @throws ParseException
     *             thrown by the parser
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static @NonNull Term makeTerm(@NonNull String content,
            @NonNull Environment env, @NonNull String context)
            throws ParseException, ASTVisitException {
        return makeTerm(content, env, context, 1, 1);
    }

    //
    // --- Visitor nature ---
    //

    /*
     * The result... fields hold intermediate calculation results.
     */
    private Term resultTerm;

    private Type resultType;

    private Modality resultModality;

    /**
     * The environment to use.
     */
    private Environment env;

    /**
     * create a new TermMaker
     * 
     * @param env
     *            the environment to look up functions, sorts, ...
     */
    public TermMaker(@NonNull Environment env) {
        this.env = env;
    }

    /*
     * Visit all subterms of a node and collect the results as an array of
     * terms.
     */
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
        String binderSymb = binderTerm.getBinderToken().image;
        Binder binder = env.getBinder(binderSymb);

        // checked elsewhere
        assert binder != null;

        binderTerm.getVariableType().visit(this);
        Type variableType = resultType;

        String variableName = binderTerm.getVariableToken().image;

        Term[] subterms = collectSubterms(binderTerm);

        try {
            resultTerm = new Binding(binder, binderTerm.getTyping().getType(),
                    variableType, variableName, subterms);
        } catch (TermException ex) {
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

    public void visit(ASTNumberLiteralTerm numberLiteralTerm)
            throws ASTVisitException {
        Function funct = env.getNumberLiteral(numberLiteralTerm
                .getNumberToken().image);
        try {
            resultTerm = new Application(funct, env.getIntType());
        } catch (TermException e) {
            throw new ASTVisitException(e, numberLiteralTerm);
        }
    }

    public void visit(ASTAsType asType) throws ASTVisitException {
        asType.getTerm().visit(this);
    }

    public void visit(ASTOperatorIdentifierTerm operatorIdentifierTerm)
            throws ASTVisitException {
        
        throw new Error(
                "This must not appear. These terms must have been resolved earlier");
    }

    public void visit(ASTListTerm listTerm) throws ASTVisitException {

        throw new Error(
                "This must not appear. These terms must have been resolved earlier");

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

    public void visit(ASTTypeApplication typeRef) throws ASTVisitException {
        List<ASTType> subty = typeRef.getArgumentTypeRefs();
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
    
    // drop the '
    public void visit(ASTTypeVar typeVar) throws ASTVisitException {
        resultType = new TypeVariable(typeVar.getTypeVarToken().image.substring(1));
    }

}
