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
import de.uka.iti.pseudo.util.AnnotatedString;

/**
 * The Class PrettyPrint provides mean to prettyprint terms while keeping the
 * information about subterms in the resulting string.
 * 
 * Parentheses are introduced only where necessary. This is done using 
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
    public static @NonNull AnnotatedString<Term> print(
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
    public static @NonNull AnnotatedString<Term> print(
            @NonNull Environment env, @NonNull Term term, boolean typed,
            boolean printFix) {
        PrettyPrint pp = new PrettyPrint(env, typed, printFix);

        try {
            term.visit(pp);
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }

        assert pp.printer.hasEmptyStack();

        return pp.printer;

    }

    /**
     * Prints the.
     * 
     * @param env
     *            the env
     * @param lterm
     *            the lterm
     * 
     * @return the annotated string< term>
     */
    public static @NonNull AnnotatedString<Term> print(
            @NonNull Environment env, @NonNull LocatedTerm lterm) {
        return print(env, lterm, false, true);
    }
    
 // TODO: Auto-generated Javadoc
    /**
     * Prints the.
     * 
     * @param env
     *            the env
     * @param lterm
     *            the lterm
     * @param typed
     *            the typed
     * @param printFix
     *            the print fix
     * 
     * @return the annotated string< term>
     */
    public static @NonNull AnnotatedString<Term> print(
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

        printer = new AnnotatedString<Term>();
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
    private AnnotatedString<Term> printer;

    /**
     * Indicator that the current subterm is to put in parentheses
     */
    private boolean inParens;
    
    // TODO DOC fertigmacen

    /*
     * Visit a subterm and put it in parens possibly.
     * 
     * If typing is switched on, many parentheses are included
     * 
     * @param subterm
     *            the subterm
     * @param precedence
     *            the precedence
     * 
     * @throws TermException
     *             the term exception
     */
    private void visitMaybeParen(Term subterm, int precedence)
            throws TermException {

        int innerPrecedence = getPrecedence(subterm);
        if (isTyped() && innerPrecedence < Integer.MAX_VALUE || !isTyped()
                && innerPrecedence < precedence) {
            inParens = true;
            subterm.visit(this);
        } else {
            subterm.visit(this);
        }

    }

    /**
     * Gets the precedence of a term. This is straight forward if it is a fixed term.
     * Then the precedence of the operator is returned.
     * 
     * In any other case the precedence is maximal ({@link Integer#MAX_VALUE}
     * This is because every infix and prefix operator binds less than other term
     * constructions (binders, applications, even modalities)
     * 
     * @param subterm
     *            the subterm to inspect
     * 
     * @return the precedence
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

    /**
     * @param variable
     * @throws TermException
     */
    public void visit(Variable variable) throws TermException {
        printer.begin(variable).append(variable.toString(isTyped())).end();
    }

    /**
     * @param modalityTerm
     * @throws TermException
     */
    public void visit(ModalityTerm modalityTerm) throws TermException {
        printer.begin(modalityTerm);
        printer.append("[ ");
        modalityTerm.getModality().visit(this);
        printer.append(" ]");
        visitMaybeParen(modalityTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.end();
    }

    /**
     * @param binding
     * @throws TermException
     */
    public void visit(Binding binding) throws TermException {
        printer.begin(binding);
        Binder binder = binding.getBinder();
        String bindname = binder.getName();
        printer.append("(").append(bindname).append(" ");
        printer.append(binding.getVariableName());
        if (isTyped())
            printer.append(" as ").append(binding.getType().toString());
        for (Term t : binding.getSubterms()) {
            printer.append("; ");
            t.visit(this);
        }
        printer.append(")");
        printer.end();
    }

    /**
     * @param application
     * @throws TermException
     */
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

    /**
     * Prints the prefix.
     * 
     * @param application
     *            the application
     * @param fixOperator
     *            the fix operator
     * 
     * @throws TermException
     *             the term exception
     */
    private void printPrefix(Application application, FixOperator fixOperator)
            throws TermException {
        Term subterm = application.getSubterm(0);
        if (printer.length() > 0 && isOperatorChar(printer.getLastCharacter()))
            printer.append(" ");
        printer.append(fixOperator.getOpIdentifier());
        visitMaybeParen(subterm, fixOperator.getPrecedence());
    }

    // keep this updated with TermParser.jj
    /**
     * Checks if is operator char.
     * 
     * @param c
     *            the c
     * 
     * @return true, if is operator char
     */
    private boolean isOperatorChar(char c) {
        return "+-<>&|=*/!^".indexOf(c) != -1;
    }

    /**
     * Prints the infix.
     * 
     * @param application
     *            the application
     * @param fixOperator
     *            the fix operator
     * 
     * @throws TermException
     *             the term exception
     */
    private void printInfix(Application application, FixOperator fixOperator)
            throws TermException {
        visitMaybeParen(application.getSubterm(0), fixOperator.getPrecedence());
        printer.append(" ").append(fixOperator.getOpIdentifier()).append(" ");
        // TODO explain
        visitMaybeParen(application.getSubterm(1),
                fixOperator.getPrecedence() + 1);
    }

    /**
     * Prints the application.
     * 
     * @param application
     *            the application
     * @param fctname
     *            the fctname
     * 
     * @throws TermException
     *             the term exception
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

    /**
     * @param schemaVariable
     * @throws TermException
     */
    public void visit(SchemaVariable schemaVariable) throws TermException {
        printer.begin(schemaVariable)
                .append(schemaVariable.toString(isTyped())).end();
    }

    /**
     * @param assignModality
     * @throws TermException
     */
    public void visit(AssignModality assignModality) throws TermException {
        printer.append(assignModality.getAssignedConstant().getName()).append(
                " := ");
        assignModality.getAssignedTerm().visit(this);
    }

    /**
     * @param compoundModality
     * @throws TermException
     */
    public void visit(CompoundModality compoundModality) throws TermException {
        compoundModality.getSubModality(0).visit(this);
        printer.append("; ");
        compoundModality.getSubModality(1).visit(this);
    }

    /**
     * @param ifModality
     * @throws TermException
     */
    public void visit(IfModality ifModality) throws TermException {
        printer.append("if ");
        ifModality.getConditionTerm().visit(this);
        printer.append(" then ");
        ifModality.getThenModality().visit(this);
        Modality elseModality = ifModality.getElseModality();
        if (elseModality != null) {
            elseModality.visit(this);
        }
        printer.append(" end");

    }

    /**
     * @param skipModality
     * @throws TermException
     */
    public void visit(SkipModality skipModality) throws TermException {
        printer.append("skip");
    }

    /**
     * @param schemaModality
     * @throws TermException
     */
    public void visit(SchemaModality schemaModality) throws TermException {
        printer.append(schemaModality.getName());
    }

    /**
     * @param whileModality
     * @throws TermException
     */
    public void visit(WhileModality whileModality) throws TermException {
        printer.append("while ");
        whileModality.getConditionTerm().visit(this);
        printer.append(" do ");
        whileModality.getBody().visit(this);
        printer.append(" end");
    }

}
