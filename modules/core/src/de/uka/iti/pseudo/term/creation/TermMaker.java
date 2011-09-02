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
package de.uka.iti.pseudo.term.creation;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.MapTypeRuleCreator;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTProperties;
import de.uka.iti.pseudo.parser.file.ASTPropertiesDeclaration;
import de.uka.iti.pseudo.parser.program.ASTAssertStatement;
import de.uka.iti.pseudo.parser.program.ASTAssignment;
import de.uka.iti.pseudo.parser.program.ASTAssignmentStatement;
import de.uka.iti.pseudo.parser.program.ASTAssumeStatement;
import de.uka.iti.pseudo.parser.program.ASTEndStatement;
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.program.ASTHavocStatement;
import de.uka.iti.pseudo.parser.program.ASTLabelStatement;
import de.uka.iti.pseudo.parser.program.ASTSchematicAssignmentStatement;
import de.uka.iti.pseudo.parser.program.ASTSkipStatement;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTExplicitVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTHeadElement;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTMapOperationTerm;
import de.uka.iti.pseudo.parser.term.ASTMapType;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaType;
import de.uka.iti.pseudo.parser.term.ASTSchemaUpdateTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
import de.uka.iti.pseudo.parser.term.ASTTypevarBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTUpdateTerm;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.HavocStatement;
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
@SuppressWarnings("nullness")
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
     * 
     * @param targetType
     *            the target type of the whole term. This type must not contain
     *            SchemaTypes
     * 
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
        TypingResolver typingResolver = new TypingResolver(env);
        astTerm.visit(typingResolver);
        astTerm = (ASTTerm) head.getWrappedElement();
        

        try {
            if(targetType != null) {
                TypeMatchVisitor matcher = new TypeMatchVisitor(new TermMatcher());
                astTerm.getTyping().getRawType().accept(matcher, targetType);
            }
        } catch (UnificationException e) {
            throw new ASTVisitException("cannot type term as " + targetType, astTerm, e);
        } catch (TermException e) {
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
     * <p>
     * The resulting term is typed as targetType. If this is not possible, an
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

    public static Update makeAndTypeUpdate(String updString, Environment env)
            throws ASTVisitException, ParseException {

        String toParse = updString + " true";

        Term t = makeAndTypeTerm(toParse, env);
        if (t instanceof UpdateTerm) {
            UpdateTerm updTerm = (UpdateTerm) t;
            return updTerm.getUpdate(); 
        }
        
        throw new ASTVisitException(updString + " does not denote a valid update");
    }
    
    /**
     * Make a statement from an AST.
     * 
     * A given statement AST is subject to an instance of this class which then
     * creates a {@link Statement} object out of the AST.
     * 
     * @param astStatement
     *            the ast of the statement to be created
     * @param linenumber the linenumber to be used for this statment
     * @param env
     *            the environment used to resolve in term making
     *            
     * @return a statement which represents astStatement
     * 
     * @throws ASTVisitException
     *             thrown on error during AST traversal.
     */
    public static Statement makeAndTypeStatement(ASTStatement astStatement,
            int linenumber, Environment env) throws ASTVisitException {
        
        TypingResolver typingResolver = new TypingResolver(env);
        
        astStatement.visit(typingResolver);
        
        TermMaker termMaker = new TermMaker(env);
        termMaker.sourceLineNumber = linenumber;
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
    private Assignment resultAssignment;

    /**
     * The environment to use.
     */
    private Environment env;
    
    /**
     * The linenumber to be set when creating statements.
     * This must be set from outside, is not changed within the visitor code
     * but given to statements when creating them.
     */
    private int sourceLineNumber;
    
    /**
     * For the resolution of identifiers it is crucial to keep track of all
     * bound variable names.
     */
	private Stack<String> boundIdentifiers =
		new Stack<String>();
    
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
            resultTerm = Application.getInst(funct, type, subterms);
        } catch (TermException e) {
            throw new ASTVisitException(applicationTerm,e );
        }
    }

    public void visit(ASTMapOperationTerm term) throws ASTVisitException {

        Type map_t = term.getMapTerm().getTyping().getType();
        assert map_t instanceof TypeApplication;

        final String operationName = (term.isLoad() ? "$load_" : "$store_")
                + ((TypeApplication) map_t).getSort().getName();

        Function operation = env.getFunction(operationName);

        // checked elsewhere
        assert operation != null;

        Term[] subterms = collectSubterms(term);

        Type type = term.getTyping().getType();
        try {
            resultTerm = Application.getInst(operation, type, subterms);
        } catch (TermException e) {
            throw new ASTVisitException(term, e);
        }
    }

    public void visit(ASTBinderTerm binderTerm) throws ASTVisitException {
        try {
            String binderSymb = binderTerm.getBinderToken().image;
            Binder binder = env.getBinder(binderSymb);

            // checked elsewhere
            assert binder != null;
            
            if(binderTerm.countBoundVariables() != 1 &&
                    binder.getArity() != 1) {
                throw new ASTVisitException("Binding more than one variable to a multi-ary binder", binderTerm);
            }
            
            int oldStackSize = boundIdentifiers.size();
            
            // Put bound variables on stack
            for (int v = 0; v < binderTerm.countBoundVariables(); v++) {
                String variableName = binderTerm.getVariableToken(v).image;
                boundIdentifiers.push(variableName);
            }
            
            // make subterms
            Term[] subterms = collectSubterms(binderTerm);
            
            // pop variables
            boundIdentifiers.setSize(oldStackSize);

            
            // one variable is mandatory: the last, the innermost one
            int innermost = binderTerm.countBoundVariables() - 1;
            Type variableType = binderTerm.getVariableTyping(innermost).getType();
            String variableName = binderTerm.getVariableToken(innermost).image;
            BindableIdentifier boundId;
            
            if(variableName.startsWith("%")) {
                boundId = SchemaVariable.getInst(variableName, variableType);
            } else {
                boundId = Variable.getInst(variableName, variableType);
            }

            resultTerm = Binding.getInst(binder, binderTerm.getTyping().getType(),
                    boundId, subterms);
            
            // Additional variables may be present: decreasingly
            for(int var = binderTerm.countBoundVariables() - 2; var >= 0; var--) {
                variableType = binderTerm.getVariableTyping(var).getType();
                variableName = binderTerm.getVariableToken(var).image;

                if(variableName.startsWith("%")) {
                    boundId = SchemaVariable.getInst(variableName, variableType);
                } else {
                    boundId = Variable.getInst(variableName, variableType);
                }

                // wrap the result so far again
                resultTerm = Binding.getInst(binder, binderTerm.getTyping().getType(),
                        boundId, new Term[] { resultTerm });
            }
            
        } catch (TermException e) {
            throw new ASTVisitException(binderTerm, e);
        }
    }
    
    public void visit(ASTTypevarBinderTerm arg) throws ASTVisitException {
        
        TypeVariableBinding.Kind kind = null;

        switch(arg.getBinderToken().kind) {
        case ParserConstants.ALL_TY: kind = TypeVariableBinding.Kind.ALL; break;
        case ParserConstants.EX_TY: kind = TypeVariableBinding.Kind.EX; break;
        default: throw new Error("The parser must not accept more than these two type var binders");
        }
        
        arg.getTerm().visit(this);
        Term subterm = resultTerm;
        
        Type boundType = arg.getBoundTyping().getType();
        
        try {
            resultTerm = TypeVariableBinding.getInst(kind, boundType, subterm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    public void visit(ASTFixTerm fixTerm) throws ASTVisitException {
        String fctName = fixTerm.getFixOperator().getName();
        Function function = env.getFunction(fctName);

        assert function != null;

        Term[] subterms = collectSubterms(fixTerm);

        Type type = fixTerm.getTyping().getType();
        try {
            resultTerm = Application.getInst(function, type, subterms);
        } catch (TermException e) {
            throw new ASTVisitException(fixTerm, e);
        }
    }

    public void visit(ASTIdentifierTerm identifierTerm)
            throws ASTVisitException {
        String name = identifierTerm.getSymbol().image;
        
        Type type = identifierTerm.getTyping().getType();

        try {
        	if(boundIdentifiers.contains(name)) {
        		resultTerm = Variable.getInst(name, type);
        	} else {
        		Function funcSymbol = env.getFunction(name);
        		if (funcSymbol != null) {
        			resultTerm = Application.getInst(funcSymbol, type);
        		} else {
        			throw new TermException("Unknown symbol: " + identifierTerm);
        		}
        	}
        } catch (TermException e) {
            throw new ASTVisitException(identifierTerm, e);
        }
    }
    
    public void visit(ASTExplicitVariableTerm explicitVariable)
            throws ASTVisitException {
        String name = explicitVariable.getVarToken().image;
        Type type = explicitVariable.getTyping().getType();
        
        resultTerm = Variable.getInst(name, type);
    }
    
    public void visit(ASTSchemaVariableTerm schemaVariableTerm)
            throws ASTVisitException {
        Type type = schemaVariableTerm.getTyping().getType();
        String name = schemaVariableTerm.getName();
        try {
            resultTerm = SchemaVariable.getInst(name, type);
        } catch (TermException e) {
            throw new ASTVisitException(schemaVariableTerm, e);
        }
    }


    public void visit(ASTNumberLiteralTerm numberLiteralTerm)
            throws ASTVisitException {
        Function funct = env.getNumberLiteral(numberLiteralTerm
                .getNumberToken().image);
        try {
            resultTerm = Application.getInst(funct, Environment.getIntType());
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
        
        Assignment[] assignments = new Assignment[children.size()-1];
        for (int i = 1; i < children.size(); i++) {
            children.get(i).visit(this);
            assignments[i-1] = resultAssignment;
        }
        
        resultTerm = UpdateTerm.getInst(new Update(assignments), term);
    }
    
    public void visit(ASTSchemaUpdateTerm arg) throws ASTVisitException {
        List<ASTElement> children = arg.getChildren();
        
        children.get(0).visit(this);
        Term term = resultTerm;
        
        String identifier = arg.getIdentifierToken().image;
        
        resultTerm = SchemaUpdateTerm.getInst(identifier, term);
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
        
        if(programTerm.hasMatchingStatement()) {
            programTerm.getMatchingStatement().visit(this);
            matchingStatement = resultStatement;
        }
        
        try {
            Token position = programTerm.getLabel();
            boolean terminating = programTerm.isTerminating();
            programTerm.getSuffixFormula().visit(this);
            Term suffixFormula = resultTerm;
            if (programTerm.isSchema()) {
                SchemaVariable sv = SchemaVariable.getInst(position.image, Environment.getBoolType());
                resultTerm = SchemaProgramTerm.getInst(sv, terminating, matchingStatement, suffixFormula);
            } else {
                Token programReference = programTerm.getProgramReferenceToken();
                Program program = env.getProgram(programReference.image);
                if(program == null)
                    throw new TermException("Unknown program '" +programReference + "'");
                int programIndex = Integer.parseInt(position.image);
                resultTerm = LiteralProgramTerm.getInst(programIndex, terminating, program, suffixFormula);
            }
        } catch (TermException e) {
            throw new ASTVisitException(programTerm, e);
        } catch (NumberFormatException e) {
            throw new ASTVisitException(programTerm, e);
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
        resultType = TypeVariable.getInst(typeVar.getTypeVarToken().image.substring(1));
    }
    
    public void visit(ASTSchemaType schemaType) throws ASTVisitException {
        resultType = SchemaType.getInst(schemaType.getSchemaTypeToken().image.substring(2));
    }

    public void visit(ASTAssertStatement arg) throws ASTVisitException {
        arg.getTerm().visit(this);
        try {
            resultStatement = new AssertStatement(sourceLineNumber, resultTerm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTAssumeStatement arg) throws ASTVisitException {
        arg.getTerm().visit(this);
        try {
            resultStatement = new AssumeStatement(sourceLineNumber, resultTerm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTAssignmentStatement arg) throws ASTVisitException {
        List<ASTElement> children = arg.getChildren();
        List<Assignment> assignments = new ArrayList<Assignment>();
        for (ASTElement child : children) {
            assert child instanceof ASTAssignment;
            child.visit(this);
            assignments.add(resultAssignment);
        }
        
        try {
            resultStatement = new AssignmentStatement(sourceLineNumber, assignments);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTSchematicAssignmentStatement arg)
            throws ASTVisitException {
        String identifier = arg.getIdentifierToken().image;
        
        resultStatement = new AssignmentStatement(sourceLineNumber, identifier);
    }
    
    public void visit(ASTEndStatement arg) throws ASTVisitException {
        try {
            resultStatement = new EndStatement(sourceLineNumber);
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
            resultStatement = new GotoStatement(sourceLineNumber, targets);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTHavocStatement arg) throws ASTVisitException {
        arg.getArgument().visit(this);
        try {
            resultStatement = new HavocStatement(sourceLineNumber, resultTerm);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    public void visit(ASTSkipStatement arg) throws ASTVisitException {
        List<ASTTerm> subterms = SelectList.select(ASTTerm.class, arg.getChildren());
        Term[] arguments = new Term[subterms.size()];
        for (int i = 0; i < arguments.length; i++) {
            subterms.get(i).visit(this);
            arguments[i] = resultTerm;
        }
        
        try {
            resultStatement = new SkipStatement(sourceLineNumber, arguments);
        } catch (TermException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    public void visit(ASTAssignment arg) throws ASTVisitException {
        
        arg.getTarget().visit(this);
        Term target = resultTerm;
        
        arg.getTerm().visit(this);
        Term value = resultTerm;

        try {
            resultAssignment = new Assignment(target, value);
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

    @Override
    public void visit(ASTProperties plugins) throws ASTVisitException {
    }

    @Override
    public void visit(ASTPropertiesDeclaration plugin) throws ASTVisitException {
    }

}
