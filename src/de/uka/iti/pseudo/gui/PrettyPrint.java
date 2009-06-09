/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.gui;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.AssignModality;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.CompoundModality;
import de.uka.iti.pseudo.term.IfModality;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.ModalityVisitor;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.SkipModality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.WhileModality;
import de.uka.iti.pseudo.term.creation.SubtermCollector;
import de.uka.iti.pseudo.util.AnnotatedStringsWithStyles;

/**
 * The Class PrettyPrint provides mean to prettyprint terms while keeping the
 * information about subterms in the resulting string.
 * 
 * Parentheses are introduced only where necessary. This is done using
 * 
 * <p>IMPORTANT! Keep the order in this visitor synchronized with the related 
 * visitors {@link SubtermCollector}
 */

public class PrettyPrint implements TermVisitor, ModalityVisitor {

    /**
     * Prints a term without explicit typing, but fix operators in prefix or
     * infix notation.
     * 
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     * 
     * @param env
     *            the env to resolve fix operators
     * @param term
     *            the term to pretty print
     * 
     * @return the annotated string
     */
    public static @NonNull AnnotatedStringsWithStyles<Term> print(
            @NonNull Environment env, @NonNull Term term) {
        return print(env, term, false, true);
    }

    /**
     * Prints a term without explicit typing.
     * 
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     * 
     * @param env
     *            the env to resolve fix operators
     * @param term
     *            the term to pretty print
     * @param typed
     *            if true, all subterms are amended with an explicit typing via
     *            "as type"
     * @param printFix
     *            if true, all fix operators which have a prefix or infix
     *            presentation are printed in such a manner
     * 
     * @return the annotated string
     */
    public static @NonNull AnnotatedStringsWithStyles<Term> print(
            @NonNull Environment env, @NonNull Term term, boolean typed,
            boolean printFix) {
        PrettyPrint pp = new PrettyPrint(env, typed, printFix);

        try {
            pp.printer.setStyle("normal");
            term.visit(pp);
            pp.printer.resetPreviousStyle();
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }

        assert pp.printer.hasEmptyStack();

        return pp.printer;

    }

    /**
     * Prints a located term without explicit typing.
     * 
     * <p>The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     * 
     * If the matching location of the located term is not
     * {@link MatchingLocation#BOTH} the sequent separator "|-" is added
     * to the string on the appropriate side.
     * 
     * @param env
     *            the environment to use
     * @param lterm
     *            the located term to print
     * 
     * @return an annotated string
     */
    public static @NonNull AnnotatedStringsWithStyles<Term> print(
            @NonNull Environment env, @NonNull LocatedTerm lterm) {
        return print(env, lterm, false, true);
    }
    
    /**
     * Prints a located term.
     * 
     * <p>The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     * 
     * If the matching location of the located term is not
     * {@link MatchingLocation#BOTH} the sequent separator "|-" is added
     * to the string on the appropriate side.
     * 
     * @param env
     *            the environment to use
     * @param lterm
     *            the located term to print
     * @param typed
     *            if true, the string will be printed typed
     * @param printFix
     *            if true print operator in infix or prefix
     *            notation
     * 
     * @return an annotated string
     */
    public static @NonNull AnnotatedStringsWithStyles<Term> print(
            @NonNull Environment env, @NonNull LocatedTerm lterm,
            boolean typed, boolean printFix) {
        PrettyPrint pp = new PrettyPrint(env, typed, printFix);

        try {
            switch (lterm.getMatchingLocation()) {
            case ANTECEDENT:
                lterm.getTerm().visit(pp);
                pp.printer.append(" |-");
                assert pp.printer.hasEmptyStack();
                return pp.printer;

            case SUCCEDENT:
                pp.printer.append("|- ");
                lterm.getTerm().visit(pp);
                assert pp.printer.hasEmptyStack();
                return pp.printer;

            case BOTH:
                lterm.getTerm().visit(pp);
                assert pp.printer.hasEmptyStack();
                return pp.printer;
            }
            // unreachable
            throw new Error();
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }
    }

    /**
     * The environment to lookup infix and prefix operators
     */
    private Environment env;

    /**
     * whether or not types are to be printed.
     */
    private boolean typed;

    /**
     * whether or not fix operators are printed as such.
     */
    private boolean printFix;

    /*
     * Instantiates a new pretty print.
     */
    private PrettyPrint(@NonNull Environment env, boolean typed,
            boolean printFix) {
        this.env = env;
        this.typed = typed;
        this.printFix = printFix;

        printer = new AnnotatedStringsWithStyles<Term>();
    }

    private boolean isTyped() {
        return typed;
    }

    private boolean isPrintingFix() {
        return printFix;
    }

    /**
     * The underlying annotating string
     */
    private AnnotatedStringsWithStyles<Term> printer;

    /**
     * Indicator that the current subterm is to put in parentheses
     */
    private boolean inParens;
    
    /*
     * Visit a subterm and put it in parens possibly.
     * 
     * Parens are included if the subterm's precedence is less than
     * the precedence of the surrounding term
     * 
     * If typing is switched on, parentheses are included if the 
     * term has a non-maximal precedence, i.e. if it is a prefixed 
     * or infixed expression
     */
    private void visitMaybeParen(Term subterm, int precedence)
            throws TermException {

        int innerPrecedence = getPrecedence(subterm);
        if ((isTyped() && innerPrecedence < Integer.MAX_VALUE)
                || (!isTyped() && innerPrecedence < precedence)) {
            inParens = true;
            subterm.visit(this);
        } else {
            subterm.visit(this);
        }

    }

    /*
     * Gets the precedence of a term. This is straight forward if it is a fixed term.
     * Then the precedence of the operator is returned.
     * 
     * In any other case the precedence is maximal ({@link Integer#MAX_VALUE}
     * This is because every infix and prefix operator binds less than other term
     * constructions (binders, applications, even modalities)
     */
    private int getPrecedence(Term subterm) {

        if (isPrintingFix() && subterm instanceof Application) {
            Application appl = (Application) subterm;
            FixOperator fix = env.getReverseFixOperator(appl.getFunction()
                    .getName());
            if (fix != null)
                return fix.getPrecedence();
        }

        return Integer.MAX_VALUE;
    }

    /*
     * Prints a term in prefix way.
     * 
     * Possibly insert an extra space if needed, that is if
     * two operators follow directly one another.
     */
    private void printPrefix(Application application, FixOperator fixOperator)
            throws TermException {
        
        assert fixOperator.getArity() == 1;
        
        Term subterm = application.getSubterm(0);
        if (printer.length() > 0 && isOperatorChar(printer.getLastCharacter()))
            printer.append(" ");
        printer.append(fixOperator.getOpIdentifier());
        visitMaybeParen(subterm, fixOperator.getPrecedence());
    }

    // keep this updated with TermParser.jj
    /**
     * Checks if a character is an operator char.
     */
    private boolean isOperatorChar(char c) {
        return "+-<>&|=*/!^".indexOf(c) != -1;
    }

    /*
     * Prints a term in infix way.
     * 
     * The first subterm is visited to be put in parens if the precedence is
     * strictly higher than that of this term.
     * 
     * The second subterm is visited to be put in parens if the precedence is
     * at least as high as that of this term.
     * 
     * Therefore plus(a,plus(b,c)) is put as a + (b + c)
     * and plus(plus(a,b),c) is put as a + b + c
     * 
     * All operators are left associative automatically.
     * 
     */
    private void printInfix(Application application, FixOperator fixOperator)
            throws TermException {
        visitMaybeParen(application.getSubterm(0), fixOperator.getPrecedence());
        printer.append(" ").append(fixOperator.getOpIdentifier()).append(" ");
        visitMaybeParen(application.getSubterm(1),
                fixOperator.getPrecedence() + 1);
    }

    /*
     * print an application in non-operator prefix form.
     */
    private void printApplication(Application application, String fctname)
            throws TermException {
        printer.append(fctname);
        List<Term> subterms = application.getSubterms();
        if (subterms.size() > 0) {
            boolean first = true;
            for (Term t : subterms) {
                printer.append(first ? "(" : ", ");
                first = false;
                t.visit(this);
            }
            printer.append(")");
        }
        if (isTyped())
            printer.append(" as " + application.getType());
    }

    //
    // Visitors
    //
    
    public void visit(Variable variable) throws TermException {
        printer.setStyle("variable");
        printer.begin(variable).append(variable.toString(isTyped())).end();
        printer.resetPreviousStyle();
    }

    public void visit(ModalityTerm modalityTerm) throws TermException {
        printer.begin(modalityTerm);
        printer.setStyle("modality");
        printer.append("[ ");
        modalityTerm.getModality().visit(this);
        printer.append(" ]");
        printer.resetPreviousStyle();
        visitMaybeParen(modalityTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.end();
    }

    public void visit(Binding binding) throws TermException {
        printer.begin(binding);
        Binder binder = binding.getBinder();
        String bindname = binder.getName();
        printer.append("(").append(bindname).append(" ");
        printer.setStyle("variable");
        printer.append(binding.getVariableName());
        if (isTyped())
            printer.append(" as ").append(binding.getType().toString());
        printer.resetPreviousStyle();
        for (Term t : binding.getSubterms()) {
            printer.append("; ");
            t.visit(this);
        }
        printer.append(")");
        printer.end();
    }

    public void visit(Application application) throws TermException {
        printer.begin(application);
        boolean isInParens = inParens;
        inParens = false;
        if (isInParens)
            printer.append("(");

        Function function = application.getFunction();
        String fctname = function.getName();

        FixOperator fixOperator = null;
        if (printFix)
            fixOperator = env.getReverseFixOperator(fctname);

        if (fixOperator != null) {
            if (isTyped())
                printer.append("(");

            if (function.getArity() == 1) {
                printPrefix(application, fixOperator);
            } else {
                printInfix(application, fixOperator);
            }

            if (isTyped())
                printer.append(") as ")
                        .append(application.getType().toString());

        } else {

            printApplication(application, fctname);

        }

        if (isInParens)
            printer.append(")");
        printer.end();
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        printer.begin(schemaVariable)
                .append(schemaVariable.toString(isTyped())).end();
    }

    public void visit(AssignModality assignModality) throws TermException {
        printer.append(assignModality.getAssignTarget().getName()).append(" := ");
        assignModality.getAssignedTerm().visit(this);
    }

    public void visit(CompoundModality compoundModality) throws TermException {
        compoundModality.getSubModality(0).visit(this);
        printer.append("; ");
        compoundModality.getSubModality(1).visit(this);
    }

    public void visit(IfModality ifModality) throws TermException {
        printer.setStyle("keyword");
        printer.append("if ");
        printer.resetPreviousStyle();
        
        ifModality.getConditionTerm().visit(this);
        
        printer.setStyle("keyword");
        printer.append(" then ");
        printer.resetPreviousStyle();
        
        ifModality.getThenModality().visit(this);
        Modality elseModality = ifModality.getElseModality();
        if (elseModality != null) {
            printer.setStyle("keyword");
            printer.append(" else ");
            printer.resetPreviousStyle();
            
            elseModality.visit(this);
        }
        
        printer.setStyle("keyword");
        printer.append(" end");
        printer.resetPreviousStyle();

    }

    public void visit(SkipModality skipModality) throws TermException {
        printer.append("skip");
    }

    public void visit(SchemaModality schemaModality) throws TermException {
        printer.append(schemaModality.getName());
    }

    public void visit(WhileModality whileModality) throws TermException {
        printer.setStyle("keyword");
        printer.append("while ");
        printer.resetPreviousStyle();
        
        whileModality.getConditionTerm().visit(this);
        if(whileModality.hasInvariantTerm()) {
            printer.setStyle("keyword");
            printer.append(" inv ");
            printer.resetPreviousStyle();
            
            whileModality.getInvariantTerm().visit(this);
        }
        printer.setStyle("keyword");
        printer.append(" do ");
        printer.resetPreviousStyle();
        
        whileModality.getBody().visit(this);
        
        printer.setStyle("keyword");
        printer.append(" end");
        printer.resetPreviousStyle();
    }

}
