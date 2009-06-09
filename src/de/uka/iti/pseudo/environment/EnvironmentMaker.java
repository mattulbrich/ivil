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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTGoalAction;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTRawTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleAdd;
import de.uka.iti.pseudo.parser.file.ASTRuleAssume;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTType;
import de.uka.iti.pseudo.parser.file.ASTTypeRef;
import de.uka.iti.pseudo.parser.file.ASTTypeVar;
import de.uka.iti.pseudo.parser.file.ASTWhereClause;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.parser.file.Token;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.SelectList;

/**
 * The Class EnvironmentMaker traverses an {@link ASTFile} to extract
 * information about sors, functions, binders, fix operators, etc. etc.
 */
public class EnvironmentMaker extends ASTFileDefaultVisitor {

    /**
     * the directory where to search for system include files.
     */
    private static File SYS_DIR = new File(System.getProperty(
            "pseudo.systemdir", "sys"));

    /**
     * The enviroment that is being built.
     */
    private Environment env;

    /**
     * The problem term possibly found in the {@link ASTFile}
     */
    private @Nullable Term problemTerm;

    /**
     * Results of various types are transferred during traversel using the
     * following fields:
     */
    private Type resultingTypeRef;
    private Term resultingTerm;
    private MatchingLocation resultingMatchingLocation;
    private WhereClause resultingWhereclause;
    private GoalAction resultingGoalAction;

    /**
     * The parser to use to parse include files
     */
    private FileParser parser;

    /**
     * Instantiates a new environment maker.
     * 
     * The file is parsed and the environment created automatically. The
     * environment has the builtin environment {@link Environment#BUILT_IN_ENV}
     * as parent.
     * 
     * @param parser
     *            the parser to use for include instructions
     * @param file
     *            the file to parse, its name is used as name for the
     *            environment
     * 
     * @throws FileNotFoundException
     *             the file to parse does not exist
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    public EnvironmentMaker(FileParser parser, File file)
            throws FileNotFoundException, ParseException, ASTVisitException {
        this(parser, file, Environment.BUILT_IN_ENV);
    }

    /**
     * Instantiates a new environment maker.
     * 
     * The file is parsed and the environment created automatically.
     * 
     * @param parser
     *            the parser to use for include instructions
     * @param file
     *            the file to parse, its name is used as name for the
     *            environment
     * @param parent
     *            the parent environment to rely upon
     * 
     * @throws FileNotFoundException
     *             the file to parse does not exist
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    private EnvironmentMaker(FileParser parser, File file, Environment parent)
            throws FileNotFoundException, ASTVisitException, ParseException {
        this(parser, parser.parseFile(file), file.getPath(), parent);

    }

    /**
     * Instantiates a new environment maker.
     * 
     * @param parser
     *            the parser to use for include instructions
     * @param astFile
     *            the ast structure to traverse
     * @param name
     *            the name of the environment
     * @param parent
     *            the parent environment to rely upon
     * 
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    private EnvironmentMaker(@NonNull FileParser parser,
            @NonNull ASTFile astFile, @NonNull String name,
            @NonNull Environment parent) throws ASTVisitException {
        this.parser = parser;
        this.env = new Environment(name, parent);
        visit(astFile);
    }

    /**
     * Instantiates a new environment maker.
     * 
     * @param parser
     *            the parser to use for include instructions
     * @param astFile
     *            the ast structure to traverse
     * @param name
     *            the name of the environment
     * 
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    public EnvironmentMaker(FileParser parser, ASTFile astFile, String name)
            throws ASTVisitException {
        this(parser, astFile, name, Environment.BUILT_IN_ENV);
    }

    /**
     * Get the environment which has been created during the constructor call
     * 
     * @return the environment
     */
    public @NonNull Environment getEnvironment() {
        return env;
    }
    
    /**
     * Gets the problem term if there is any in the environment.
     * 
     * Returns null if the environment does not define a problem term.
     * 
     * @return the problem term, possibly null
     */
    public @Nullable Term getProblemTerm() {
        return problemTerm;
    }


    /**
     * Strip quotes from a string
     * 
     * @param s
     *            some string with length >= 2
     * 
     * @return the string with first and last character removed
     */
    private String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    /*
     * Make file name for an include. Leading $ is replaced by the system
     * directory.
     */
    /**
     * Mk file.
     * 
     * @param toplevel
     *            the toplevel
     * @param filename
     *            the filename
     * 
     * @return the file
     */
    private File mkFile(String toplevel, String filename) {
        File ret;
        if (filename.charAt(0) == '$') {
            ret = new File(SYS_DIR, filename.substring(1));
        } else {
            ret = new File(new File(toplevel).getParentFile(), filename);
        }
        return ret;
    }

    /*
     * default behaviour
     * 
     * visit children
     */
    protected void visitDefault(ASTFileElement arg) throws ASTVisitException {
        for (ASTFileElement child : arg) {
            child.visit(this);
        }
    }

    /*
     * do in the following order
     * - includes
     * - sorts
     * - functions
     * - binders
     * - rules
     * - problem term
     */
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
        if (problem != null) {
            problem.visit(this);
            problemTerm = resultingTerm;
        }

    }

    /*
     * create a new EnvironmentMaker for every include directive and wrap
     * into a new Enviroment every time.
     * update the parent field of env for each include. 
     */
    public void visit(ASTIncludeDeclarationBlock arg) throws ASTVisitException {

        for (Token token : arg.getIncludeStrings()) {
            String filename = stripQuotes(token.image);
            File file = mkFile(arg.getFileName(), filename);
            try {
                EnvironmentMaker includeMaker = new EnvironmentMaker(parser,
                        file, env.getParent());
                env.setParent(includeMaker.getEnvironment());
            } catch (FileNotFoundException e) {
                throw new ASTVisitException("Cannot include " + file
                        + " (not found)", arg, e);
            } catch (ParseException e) {
                throw new ASTVisitException("Error while parsing file " + file,
                        arg, e);
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }
        }

    }

    /*
     * create a new sort in env.
     * Do some basic testing:
     * - nullary assignables
     * - no type vars in assignables
     * - arities of fixies
     */
    public void visit(ASTSortDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        int arity = arg.getTypeVariables().size();

        try {
            env.addSort(new Sort(name, arity, arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * create a new Function in env
     * rely upon results from children. 
     */
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

        if (arg.isAssignable()) {
            if (arity != 0)
                throw new ASTVisitException("Assignable operator " + name
                        + " is not nullary", arg);

            Set<TypeVariable> typVars = TypeVariableCollector.collect(resultTy);

            if (!typVars.isEmpty())
                throw new ASTVisitException("Type of assignable operator "
                        + name + " contains free type variables " + typVars,
                        arg);
        }

        try {
            env.addFunction(new Function(name, resultTy, argTy, arg.isUnique(),
                    arg.isAssignable(), arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }

        if (arg.isInfix()) {
            if (arity != 2)
                throw new ASTVisitException("Arity of infix operator " + name
                        + " is not 2", arg);

            String infix = arg.getOperatorIdentifier().image;
            int precedence = Integer.parseInt(arg.getPrecedence().image);
            try {
                env.addInfixOperator(new FixOperator(name, infix, precedence,
                        2, arg));
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }
        }

        if (arg.isPrefix()) {
            if (arity != 1)
                throw new ASTVisitException("Arity of prefix operator " + name
                        + " is not 1", arg);

            String prefix = arg.getOperatorIdentifier().image;
            int precedence = Integer.parseInt(arg.getPrecedence().image);
            try {
                env.addPrefixOperator(new FixOperator(name, prefix, precedence,
                        1, arg));
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }
        }
    }

    /*
     * create a binder 
     */
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

        try {
            env.addBinder(new Binder(name, rangeTy, varTy, domTy, arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * collect all information for a rule definition and define it in env 
     */
    public void visit(ASTRule arg) throws ASTVisitException {

        try {

            String name = arg.getName().image;
            List<ASTFileElement> children = arg.getChildren();

            List<LocatedTerm> assumes = new ArrayList<LocatedTerm>();
            {
                List<ASTRuleAssume> assumeASTs = SelectList.select(
                        ASTRuleAssume.class, children);
                for (ASTRuleAssume assume : assumeASTs) {
                    assume.visit(this);
                    assumes.add(new LocatedTerm(resultingTerm,
                            resultingMatchingLocation));
                }
            }

            LocatedTerm find;
            {
                List<ASTRuleFind> findASTs = SelectList.select(
                        ASTRuleFind.class, children);
                if (findASTs.size() != 1) {
                    throw new ASTVisitException(
                            "There is not exactly one find clause.", arg);
                }
                ASTRuleFind astFind = findASTs.get(0);
                astFind.visit(this);
                find = new LocatedTerm(resultingTerm, resultingMatchingLocation);
            }

            List<WhereClause> wheres = new ArrayList<WhereClause>();
            {
                List<ASTWhereClause> whereASTs = SelectList.select(
                        ASTWhereClause.class, children);
                for (ASTWhereClause where : whereASTs) {
                    where.visit(this);
                    wheres.add(resultingWhereclause);
                }
            }

            List<GoalAction> actions = new ArrayList<GoalAction>();
            {
                List<ASTGoalAction> actionASTs = SelectList.select(
                        ASTGoalAction.class, children);
                for (ASTGoalAction action : actionASTs) {
                    action.visit(this);
                    actions.add(resultingGoalAction);
                }
            }
            
            
            
            Map<String, String> properties = new HashMap<String, String>();
            {
                for (Pair<Token, Token> prop : arg.getProperties()) {
                    properties.put(prop.fst().image, stripQuotes(prop.snd().image));
                }
                Token description = arg.getDescription();
                if(description != null)
                    properties.put("description", stripQuotes(description.image));
                
            }

            Rule rule = new Rule(name, assumes, find, wheres, actions, properties);
            try {
                env.addRule(rule);
            } catch (EnvironmentException e) {
                throw new ASTVisitException(e);
            }

        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * make a where clause
     * report if where condition is unknown. 
     */
    public void visit(ASTWhereClause arg) throws ASTVisitException {

        String identifier = arg.getIdentifier().image;

        WhereCondition where = WhereCondition.getWhereCondition(identifier);
        if (where == null)
            throw new ASTVisitException("Unknown where condition: "
                    + identifier, arg);

        List<ASTRawTerm> raws = SelectList.select(ASTRawTerm.class, arg
                .getChildren());
        Term[] terms = new Term[raws.size()];
        for (int i = 0; i < terms.length; i++) {
            raws.get(i).visit(this);
            terms[i] = resultingTerm;
        }

        try {
            resultingWhereclause = new WhereClause(where, terms);
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }

    }

    /*
     * collect children an make a new goal action
     */
    public void visit(ASTGoalAction arg) throws ASTVisitException {
        super.visit(arg);

        String kind = arg.getGoalKind().image;
        Token nameToken = arg.getName();
        String name = nameToken == null ? null : nameToken.image;
        
        List<Term> addAntecendent = new ArrayList<Term>();
        List<Term> addSuccendent = new ArrayList<Term>();
        Term replaceWith = null;

        for(ASTRuleAdd add : SelectList.select(ASTRuleAdd.class, arg.getChildren())) {
            add.visit(this);
            if(resultingMatchingLocation == MatchingLocation.ANTECEDENT)
                addAntecendent.add(resultingTerm);
            else
                addSuccendent.add(resultingTerm);
        }
        
        for(ASTRuleReplace replace : SelectList.select(ASTRuleReplace.class, arg.getChildren())) {
            assert replaceWith == null;
            replace.visit(this);
            replaceWith = resultingTerm;
        }

        try {
            resultingGoalAction = new GoalAction(kind, name, replaceWith, addAntecendent, addSuccendent);
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * Type application
     */
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
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * type variable
     */
    public void visit(ASTTypeVar arg) throws ASTVisitException {
        resultingTypeRef = new TypeVariable(arg.getTypeVarToken().image
                .substring(1));
    }

    /*
     * transform a ASTTerm to a Term.
     */
    public void visit(ASTRawTerm arg) throws ASTVisitException {
        Token token = arg.getTermToken();
        String content = stripQuotes(token.image);
        try {
            resultingTerm = TermMaker.makeTerm(content, env, arg.getFileName(),
                    token.beginLine, token.beginColumn);
        } catch (de.uka.iti.pseudo.parser.term.ParseException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * store the location for a located term.
     */
    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        super.visit(arg);
        resultingMatchingLocation = arg.getMatchingLocation();
    }

}