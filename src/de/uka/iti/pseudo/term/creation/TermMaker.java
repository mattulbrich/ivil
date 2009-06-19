/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTAssertStatement;
import de.uka.iti.pseudo.parser.program.ASTAssignmentStatement;
import de.uka.iti.pseudo.parser.program.ASTAssumeStatement;
import de.uka.iti.pseudo.parser.program.ASTEndStatement;
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.program.ASTLabelStatement;
import de.uka.iti.pseudo.parser.program.ASTSkipStatement;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTHeadElement;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;
import de.uka.iti.pseudo.parser.term.ASTProgramUpdate;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
import de.uka.iti.pseudo.parser.term.ASTUpdateTerm;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.ProgramUpdate;
import de.uka.iti.pseudo.term.SchemaProgram;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.SelectList;

/**
 * This class has two purposes:
 * 
 * It holds the {@link #makeTerm(String, Environment)} methods (and similar)
 * that allow to create types from strings.
 * 
 * It is a AST visitor that creates a term out of a ASTTerm object.
 * 
 * It has got similar abilities for types.
 * 
 * It can handle single statements which appear in program terms but cannot deal
 * with statement lists which may have symbolic labels and other stuff.
 */
public class TermMaker extends ASTDefaultVisitor {

    //
    // --- Static translation nature
    //

    /**
     * Make a term from a ast term object.
     * 
     * The AST must already have been visited by a {@link TypingResolver} and
     * the typings must have been set.
     *  
     * @param astTerm
     *            the term represented in an ast.
     * @param env
     *            the environment to use
     * @return a term representing the ast
     * 
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static @NonNull Term makeTerm(ASTTerm astTerm,
            @NonNull Environment env) throws ASTVisitException {

        TermMaker termMaker = new TermMaker(env);
        astTerm.visit(termMaker);

        return termMaker.resultTerm;
        
    }
    
    /**
     * Make a term from a ast term object.
     * 
     * The AST must NOT have been visited by a {@link TypingResolver}.
     *  
     * @param astTerm
     *            the term represented in an ast.
     * @param env
     *            the environment to use
     * @return a term representing the ast
     * 
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static @NonNull Term makeAndTypeTerm(ASTTerm astTerm,
            @NonNull Environment env, Type targetType) throws ASTVisitException {

        // We have to embed the AST into a container because the structure may
        // change if it is a ASTListTerm.
        ASTHeadElement head = new ASTHeadElement(astTerm);
        TypingResolver typingResolver = new TypingResolver(env, new TypingContext());
        astTerm.visit(typingResolver);
        astTerm = (ASTTerm) head.getWrappedElement();
        
        try {
            if(targetType != null)
                typingResolver.getTypingContext().solveConstraint(astTerm.getTyping().getRawType(), targetType);
        } catch (UnificationException e) {
            throw new ASTVisitException("cannot type term as " + targetType, astTerm, e);
        }
        
        // ast.dumpTree();

        TermMaker termMaker = new TermMaker(env);
        astTerm.visit(termMaker);

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
    public static @NonNull Term makeAndTypeTerm(@NonNull String content,
            @NonNull Environment env) throws ParseException, ASTVisitException {
        return makeAndTypeTerm(content, env, "");
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
    public static @NonNull Term makeAndTypeTerm(@NonNull String content,
            @NonNull Environment env, @NonNull String context) 
            throws ParseException, ASTVisitException {
        return makeAndTypeTerm(content, env, context, null);
    }
    
    
    /**
     * Make a term from a string.
     * 
     * A parser is created, a term is parsed the AST is then subjected to a
     * {@link TypingResolver} visitor that infers the necessary typing
     * information. An instance of this class then creates a {@link Term} object
     * out of the AST.
     * 
     * <p>The resulting term is typed as targetType. If this is not possible, an
     * exception is thrown.
     * 
     * @param content
     *            the string to parse
     * @param env
     *            the environment
     * @param context
     *            the context name to be reported as file name to the parser
     * @param targetType
     *            the target type of the whole term.
     * 
     * @return a term representing the string, it has type targetType
     * 
     * @throws ParseException
     *             thrown by the parser
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static @NonNull Term makeAndTypeTerm(@NonNull String content,
            @NonNull Environment env, @NonNull String context, @Nullable Type targetType)
            throws ParseException, ASTVisitException {
        
        Parser parser = new Parser();
        ASTTerm ast = parser.parseTerm(new StringReader(content), context);

        // ast.dumpTree();

        return makeAndTypeTerm(ast, env, targetType);
    }
    
    /**
     * Make a type from a string.
     * 
     * A parser is created, a term is parsed and then the AST is subject to an
     * instance of this class then creates a {@link Type} object out of the AST.
     * 
     * @param typeString
     *            the string to be parsed
     * @param env
     *            the environment to resovle type names from
     *            
     * @return a type which represents typeString
     * 
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     * @throws ParseException
     *             thrown by the parser
     */
    public static Type makeType(String typeString, Environment env) throws ASTVisitException, ParseException {
        
        Parser parser = new Parser(new StringReader(typeString));

        ASTType ast = parser.TypeRef();

        return makeType(ast, env);
    }
    
    /**
     * Make a type from an AST.
     * 
     * A given parsed type AST is subject to an instance of this class which then 
     * creates a {@link Type} object out of the AST.
     * 
     * @param astType
     *            the ast of the type to be created
     * @param env
     *            the environment to resovle type names from
     *            
     * @return a type which represents astType
     * 
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static Type makeType(ASTType astType, Environment env) throws ASTVisitException {
        
        TermMaker termMaker = new TermMaker(env);
        astType.visit(termMaker);

        return termMaker.resultType;
    }
    
    /**
     * Make a statement from an AST.
     * 
     * A given statement AST is subject to an instance of this class which then
     * creates a {@link Statement} object out of the AST.
     * 
     * @param astStatement
     *            the ast of the statement to be created
     * @param env
     *            the environment used to resolve in term making
     *            
     * @return a statement which represents astStatement
     * 
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static Statement makeAndTypeStatement(ASTStatement astStatement,
            Environment env) throws ASTVisitException {
        
        TypingResolver typingResolver = new TypingResolver(env, new TypingContext());
        
        for (ASTElement child : astStatement.getChildren()) {
            child.visit(typingResolver);
        }
        
        TermMaker termMaker = new TermMaker(env);
        astStatement.visit(termMaker);
        
        assert termMaker.resultStatement != null;
        
        return termMaker.resultStatement;
    }



    //
    // --- Visitor nature ---
    //

    /*
     * default behaviour: do nothing
     */
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
    }

    /*
     * The result... fields hold intermediate calculation results.
     */
    private Term resultTerm;
    private Type resultType;
    private Statement resultStatement;
    private ProgramUpdate resultProgramUpdate;

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
            throw new ASTVisitException(applicationTerm,e );
        }
    }

    public void visit(ASTBinderTerm binderTerm) throws ASTVisitException {
        try {
            String binderSymb = binderTerm.getBinderToken().image;
            Binder binder = env.getBinder(binderSymb);

            // checked elsewhere
            assert binder != null;

            Type variableType = binderTerm.getVariableTyping().getType();
            String variableName = binderTerm.getVariableToken().image;
            BindableIdentifier boundId;

            if(variableName.startsWith("%")) {
                boundId = new SchemaVariable(variableName, variableType);
            } else {
                boundId = new Variable(variableName, variableType);
            }

            Term[] subterms = collectSubterms(binderTerm);


            resultTerm = new Binding(binder, binderTerm.getTyping().getType(),
                    boundId, subterms);
        } catch (TermException e) {
            throw new ASTVisitException(binderTerm, e);
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
            throw new ASTVisitException(fixTerm, e);
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
            throw new ASTVisitException(identifierTerm, e);
        }
    }
    
    public void visit(ASTSchemaVariableTerm schemaVariableTerm)
            throws ASTVisitException {
        Type type = schemaVariableTerm.getTyping().getType();
        String name = schemaVariableTerm.getName();
        try {
            resultTerm = new SchemaVariable(name, type);
        } catch (TermException e) {
            throw new ASTVisitException(schemaVariableTerm, e);
        }
    }


    public void visit(ASTNumberLiteralTerm numberLiteralTerm)
            throws ASTVisitException {
        Function funct = env.getNumberLiteral(numberLiteralTerm
                .getNumberToken().image);
        try {
            resultTerm = new Application(funct, Environment.getIntType());
        } catch (TermException e) {
            throw new ASTVisitException(numberLiteralTerm, e);
        }
    }

    public void visit(ASTAsType asType) throws ASTVisitException {
        asType.getTerm().visit(this);
    }
    
    // child 0 is childterm;
    // child 1 ... are assignments
    public void visit(ASTUpdateTerm arg) throws ASTVisitException {
        List<ASTElement> children = arg.getChildren();
        
        children.get(0).visit(this);
        Term term = resultTerm;
        
        AssignmentStatement[] assignments = new AssignmentStatement[children.size()-1];
        for (int i = 1; i < children.size(); i++) {
            children.get(i).visit(this);
            assignments[i-1] = (AssignmentStatement) resultStatement;
        }
        
        resultTerm = new UpdateTerm(assignments, term);
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

    public void visit(ASTProgramTerm programTerm) throws ASTVisitException {

        Statement matchingStatement = null;
        List<ProgramUpdate> updates = new ArrayList<ProgramUpdate>(); 
        
        if(programTerm.hasMatchingStatement()) {
            programTerm.getMatchingStatement().visit(this);
            matchingStatement = resultStatement;
        } else {
            for (ASTProgramUpdate child : 
                SelectList.select(ASTProgramUpdate.class, programTerm.getChildren())) {
                child.visit(this);
                updates.add(resultProgramUpdate);
            }
        }
        
        try {
            Token position = programTerm.getLabel();
            boolean terminating = programTerm.isTerminating();
            if (programTerm.isSchema()) {
                SchemaVariable sv = new SchemaVariable(position.image, Environment.getBoolType());
                resultTerm = new SchemaProgram(sv, terminating, matchingStatement);
            } else {
                resultTerm = new LiteralProgramTerm(position.image, terminating, updates);
            }
        } catch (TermException e) {
            throw new ASTVisitException(programTerm, e);
        }
        
    }
    
    public void visit(ASTProgramUpdate arg) throws ASTVisitException {
        try {
            int position = Integer.parseInt(arg.getPosition().image);
            arg.getChildren().get(0).visit(this);
            resultProgramUpdate = new ProgramUpdate(position, resultStatement);
        } catch (Exception e) {
            throw new ASTVisitException(arg, e);
        } 
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
            throw new ASTVisitException(typeRef, e);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(typeRef, e);
        }
    }
    
    // drop the '
    public void visit(ASTTypeVar typeVar) throws ASTVisitException {
        resultType = new TypeVariable(typeVar.getTypeVarToken().image.substring(1));
    }

    // we only support matchable statements: source is no such statement.
    
    public void visit(ASTAssertStatement arg) throws ASTVisitException {
        arg.getTerm().visit(this);
        try {
            resultStatement = new AssertStatement(resultTerm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTAssumeStatement arg) throws ASTVisitException {
        arg.getTerm().visit(this);
        try {
            resultStatement = new AssumeStatement(resultTerm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTEndStatement arg) throws ASTVisitException {
        arg.getTerm().visit(this);
        try {
            resultStatement = new EndStatement(resultTerm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTGotoStatement arg) throws ASTVisitException {
        List<ASTTerm> subterms = SelectList.select(ASTTerm.class, arg.getChildren());
        Term[] targets = new Term[subterms.size()];
        for (int i = 0; i < targets.length; i++) {
            subterms.get(i).visit(this);
            targets[i] = resultTerm;
        }
        
        try {
            resultStatement = new GotoStatement(targets);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTSkipStatement arg) throws ASTVisitException {
        resultStatement = new SkipStatement();
    }
    
    public void visit(ASTAssignmentStatement arg) throws ASTVisitException {
        
        arg.getTarget().visit(this);
        Term target = resultTerm;
        
        arg.getTerm().visit(this);
        Term value = resultTerm;

        try {
            resultStatement = new AssignmentStatement(target, value);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    /*
     * return the statement which is wrapped by the label
     */
    public void visit(ASTLabelStatement arg) throws ASTVisitException {
        arg.getChildren().get(0).visit(this);
    }

}
