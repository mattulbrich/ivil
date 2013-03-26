/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.prettyprint;

import java.util.List;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.AnnotatedString.Style;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.TypeVariableBinding;
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
import de.uka.iti.pseudo.term.statement.StatementVisitor;

// TODO Documentation needed
/**
 * The Class PrettyPrint provides mean to prettyprint terms while keeping the
 * information about subterms in the resulting string.
 *
 * Parentheses are introduced only where necessary. This is done using
 */
class PrettyPrintVisitor implements TermVisitor, StatementVisitor {

    private final PrettyPrint pp;
    private final Environment env;
    private final PrettyPrintLayouter printer;

    /**
     * Indicator that the current subterm is to be put in parentheses
     */
    private boolean inParens;

    public PrettyPrintVisitor(PrettyPrint pp, PrettyPrintLayouter printer) {
        this.pp = pp;
        this.env = pp.getEnvironment();
        this.printer = printer;
    }

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
        if ((pp.isTyped() && innerPrecedence < Integer.MAX_VALUE)
                || (!pp.isTyped() && innerPrecedence < precedence)) {
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

        if (pp.isPrintingFix() && subterm instanceof Application) {
            Application appl = (Application) subterm;
            FixOperator fix = env.getReverseFixOperator(appl.getFunction()
                    .getName());
            if (fix != null) {
                return fix.getPrecedence();
            }
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

        if (isOperatorChar(printer.getLastCharacter())) {
            printer.append(" ");
        }

        printer.append(fixOperator.getOpIdentifier());
        printer.beginTerm(0);
        visitMaybeParen(subterm, fixOperator.getPrecedence());
        printer.endTerm();
    }

    /*
     * Try to print a term using the registered pretty print plugins.
     *
     * the first which returns true has successfully printed the term.
     * Processing is stopped and true returned
     */
    private boolean printByPlugins(Term term) throws TermException {
        if(pp.isPrintingPlugins()) {
            for (PrettyPrintPlugin plugin : pp.getPrettyPrinterPlugins()) {
                boolean res = plugin.possiblyPrettyPrintTerm(term, this, pp, printer);
                if(res) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Append a name to the output stream.
     *
     * If the plugin mechanism is active, then the registered plugins are
     * queried on a replacement. For instance, generated prefixes can be
     * removed, etc.
     *
     * If a replacement is found, it is printed. If not so, or if plugins are
     * deactivated, the argument is printed.
     *
     * @param name
     *            the name to print or to replace.
     */
    /* package visibile, used in the visitor */
    void appendName(String name) {
        if(pp.isPrintingPlugins()) {
            for (PrettyPrintPlugin plugin : pp.getPrettyPrinterPlugins()) {
                String nameReplacement = plugin.getReplacementName(name);
                if(nameReplacement != null) {
                    printer.append(nameReplacement);
                    return;
                }
            }
        }

        // else: use the real name.
        printer.append(name);
    }

    // keep this updated with TermParser.jj
    /**
     * Checks if a character is an operator char.
     */
    private boolean isOperatorChar(char c) {
        return "+-<>&|=*/!^@.:".indexOf(c) != -1;
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
        printer.beginBlock(0);
        printer.indentBlock(0, 2);
        printer.beginTerm(0);
        visitMaybeParen(application.getSubterm(0), fixOperator.getPrecedence());
        printer.endTerm();

        printer.breakBlock(1, 0).append(fixOperator.getOpIdentifier()).append(" ");

        printer.beginTerm(1);
        visitMaybeParen(application.getSubterm(1), fixOperator.getPrecedence() + 1);
        printer.endTerm();
        printer.endBlock();
    }

    /*
     * print an application in non-operator prefix form.
     */
    private void printApplication(Application application, String fctname)
            throws TermException {
        boolean assignable = application.getFunction().isAssignable();
        printer.beginBlock(fctname.length() + 1);
        if(assignable) {
            printer.setStyle(Style.ASSIGNABLE);
        }
        appendName(fctname);
        List<Term> subterms = application.getSubterms();
        if (subterms.size() > 0) {
            for (int i = 0; i < subterms.size(); i++) {
                if(i == 0) {
                    printer.append("(");
                }
                else {
                    printer.append(",").breakBlock(1,0);
                }
                printer.beginTerm(i);
                subterms.get(i).visit(this);
                printer.endTerm();
            }
            printer.append(")");
        }
        if (pp.isTyped()) {
            printer.setStyle(Style.TYPE);
            printer.append(" as " + application.getType());
            printer.resetPreviousStyle();
        }
        if(assignable) {
            printer.resetPreviousStyle();
        }
        printer.endBlock();
    }

    //
    // Visitors
    //

    @Override
    public void visit(Variable variable) throws TermException {
        printer.setStyle(Style.VARIABLE);
        appendName(variable.getName());
        if(pp.isTyped()) {
            printer.setStyle(Style.TYPE);
            printer.append(" as " + variable.getType());
            printer.resetPreviousStyle();
        }
        printer.resetPreviousStyle();
    }

    @Override
    public void visit(Binding binding) throws TermException {
        if(!printByPlugins(binding)) {
            Binder binder = binding.getBinder();
            String bindname = binder.getName();
            printer.beginBlock(bindname.length() + 1);
            printer.append("(").append(bindname).append(" ");
            printer.setStyle(Style.VARIABLE);
            appendName(binding.getVariableName());
            printer.resetPreviousStyle();
            if (pp.isTyped()) {
                printer.setStyle(Style.TYPE);
                printer.append(" as ").append(binding.getVariableType().toString());
                printer.resetPreviousStyle();
            }
            int i = 0;
            for (Term t : binding.getSubterms()) {
                printer.append("; ");
                printer.beginTerm(i);
                t.visit(this);
                printer.endTerm();
                i++;
            }
            printer.append(")");
            printer.endBlock();
        }
    }

    @Override
    public void visit(TypeVariableBinding typeVariableBinding) throws TermException {
        String bindString = typeVariableBinding.getKind().toString();
        String typevar = typeVariableBinding.getBoundType().toString();
        printer.beginBlock(PrettyPrintLayouter.DEFAULT_INDENTATION);
        printer.append("(").append(bindString).append(" ").append(typevar).append(";").
            breakBlock(1, 0);
        printer.beginTerm(0);
        typeVariableBinding.getSubterm(0).visit(this);
        printer.endTerm();
        printer.append(")");
        printer.endBlock();
    }

    @Override
    public void visit(Application application) throws TermException {
        if(!printByPlugins(application)) {
            boolean isInParens = inParens;
            inParens = false;
            if (isInParens) {
                printer.append("(");
            }

            Function function = application.getFunction();
            String fctname = function.getName();

            FixOperator fixOperator = null;
            if (pp.isPrintingFix()) {
                fixOperator = env.getReverseFixOperator(fctname);
            }

            if (fixOperator != null) {
                if (pp.isTyped()) {
                    printer.append("(");
                }

                if (function.getArity() == 1) {
                    printPrefix(application, fixOperator);
                } else {
                    printInfix(application, fixOperator);
                }

                if (pp.isTyped()) {
                    printer.append(")");
                    printer.setStyle(Style.TYPE);
                    printer.append(" as ")
                    .append(application.getType().toString());
                    printer.resetPreviousStyle();
                }

            } else {

                printApplication(application, fctname);

            }

            if (isInParens) {
                printer.append(")");
            }
        }
    }

    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        printer.append(schemaVariable.toString(pp.isTyped()));
    }

    @Override
    public void visit(LiteralProgramTerm litProgTerm) throws TermException {
        printer.setStyle(Style.PROGRAM);
        printer.append(litProgTerm.getModality().getOpeningDelimiter());
        printer.append(" " + Integer.toString(litProgTerm.getProgramIndex())
                + "; " + litProgTerm.getProgram() + " ");
        printer.append(litProgTerm.getModality().getClosingDelimiter());
        printer.beginTerm(0);
        visitMaybeParen(litProgTerm.getSuffixTerm(), Integer.MAX_VALUE);
        printer.endTerm();
        printer.resetPreviousStyle();
    }

    @Override
    public void visit(SchemaProgramTerm schemaProgramTerm)
            throws TermException {
        printer.setStyle(Style.PROGRAM);
        printer.append(schemaProgramTerm.getModality().getOpeningDelimiter()).append(" ");
        schemaProgramTerm.getSchemaVariable().visit(this);
        if(schemaProgramTerm.hasMatchingStatement()) {
            printer.append(" : " + schemaProgramTerm.getMatchingStatement().toString(pp.isTyped()));
        }
        printer.append(" ").append(schemaProgramTerm.getModality().getClosingDelimiter());
        printer.beginTerm(0);
        schemaProgramTerm.getSuffixTerm().visit(this);
        printer.endTerm();
        printer.resetPreviousStyle();
    }

    @Override
    public void visit(UpdateTerm updateTerm) throws TermException {
        printer.beginBlock(1);
        printer.setStyle(Style.UPDATE);
        printer.append("{ ");

        List<Assignment> assignments = updateTerm.getAssignments();
        visit(assignments);

        printer.append(" }").resetPreviousStyle().
            breakBlock(0, PrettyPrintLayouter.DEFAULT_INDENTATION);
        printer.beginTerm(0);
        visitMaybeParen(updateTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.endTerm();
        printer.endBlock();
    }

    /*
     * Try to print an update using the registered pretty print plugins.
     *
     * the first which returns true has successfully printed the term.
     * Processing is stopped and true returned
     */
//    private boolean printByPlugins(Update update) throws TermException {
//        if(pp.isPrintingPlugins()) {
//            for (PrettyPrintPlugin plugin : pp.getPrettyPrinterPlugins()) {
//                boolean res = plugin.possiblyPrettyPrintUpdate(update, this, pp, printer);
//                if(res)
//                    return true;
//            }
//        }
//        return false;
//    }


    @Override
    public void visit(SchemaUpdateTerm schUpdateTerm) throws TermException {
        printer.setStyle(Style.UPDATE);
        printer.append("{ " + schUpdateTerm.getSchemaIdentifier());
        if(schUpdateTerm.isOptional()) {
            printer.append(" ?}");
        } else {
            printer.append(" }");
        }
        printer.resetPreviousStyle();
        printer.beginTerm(0);
        visitMaybeParen(schUpdateTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.endTerm();
    }


    public PrettyPrintLayouter getPrinter() {
        return printer;
    }

    /*
     * A statement is not a term, but we want to be able to print it also.
     */

    public void visitStatement(String keyword, Statement statement) throws TermException {
        printer.setStyle(Style.STATEMENT);
        printer.setStyle(Style.KEYWORD);
        appendName(keyword);
        printer.resetPreviousStyle();
        printer.append(" ");

        List<Term> subterms = statement.getSubterms();
        for (int i = 0; i < subterms.size(); i++) {
            printer.beginTerm(0);
            subterms.get(i).visit(this);
            printer.endTerm();
            if(i != subterms.size()-1) {
                printer.append(", ");
            }
        }
        printer.resetPreviousStyle();
    }

    @Override
    public void visit(AssertStatement assertStatement)
            throws TermException {
        visitStatement("assert", assertStatement);
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement)
            throws TermException {
        printer.setStyle(Style.STATEMENT);
        List<Assignment> assignments = assignmentStatement.getAssignments();
        visit(assignments);
        printer.resetPreviousStyle();
    }

    // used by AssignmentStatement, UpdateTerm and for text instantiation.
    public void visit(List<Assignment> assignments)
            throws TermException {

        printer.beginBlock(0);
        printer.indentBlock(0, 3);
        for (int i = 0; i < assignments.size(); i++) {
            if(i > 0) {
                printer.breakBlock(1, 0).append("|| ");
            }
            Assignment assignment = assignments.get(i);
            appendName(assignment.getTarget().toString(false));
            printer.append(" := ");
            printer.beginTerm(i + 1);
            assignment.getValue().visit(this);
            printer.endTerm();
        }
        printer.endBlock();
    }

    @Override
    public void visit(AssumeStatement assumeStatement)
            throws TermException {
        visitStatement("assume", assumeStatement);
    }

    @Override
    public void visit(EndStatement endStatement) throws TermException {
        visitStatement("end", endStatement);
    }

    @Override
    public void visit(GotoStatement gotoStatement)
            throws TermException {
        visitStatement("goto", gotoStatement);
    }

    @Override
    public void visit(SkipStatement skipStatement)
            throws TermException {
        visitStatement("skip", skipStatement);
    }

    @Override
    public void visit(HavocStatement havocStatement)
            throws TermException {
        visitStatement("havoc", havocStatement);
    }

}
