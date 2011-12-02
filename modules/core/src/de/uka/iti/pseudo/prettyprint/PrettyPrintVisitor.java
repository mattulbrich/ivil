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
package de.uka.iti.pseudo.prettyprint;

import java.util.List;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
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
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;

// TODO Documentation needed
/**
 * The Class PrettyPrint provides mean to prettyprint terms while keeping the
 * information about subterms in the resulting string.
 * 
 * Parentheses are introduced only where necessary. This is done using
 */
class PrettyPrintVisitor implements TermVisitor, StatementVisitor {
    
    private PrettyPrint pp;
    private Environment env;
    private AnnotatedStringWithStyles<TermTag> printer;
    
    /**
     * stores the currently used term tag
     */
    private TermTag currentTermTag; 
    
    /**
     * we need to keep track which is the index of the currently
     * printed subterm wrt. its enclosing term. The order may differ from
     * natural order, e.g. for updates or for plugin printed terms.
     */
    private int currentSubTermIndex = -1;
    
    /**
     * Indicator that the current subterm is to be put in parentheses
     */
    private boolean inParens;

    public PrettyPrintVisitor(PrettyPrint pp, AnnotatedStringWithStyles<TermTag> printer) {
        this.pp = pp;
        this.env = pp.getEnvironment();
        this.printer = printer;
    }
    
    private TermTag begin(Term term) throws TermException {
        TermTag oldTag = currentTermTag;
        
        if(oldTag == null) {
            currentTermTag = new TermTag(term);
        } else {
            currentTermTag = oldTag.derive(currentSubTermIndex);
        }
        printer.begin(currentTermTag);
        
        return oldTag;
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
        currentSubTermIndex = 0;
        visitMaybeParen(subterm, fixOperator.getPrecedence());
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
                if(res)
                    return true;
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
        currentSubTermIndex = 0;
        visitMaybeParen(application.getSubterm(0), fixOperator.getPrecedence());
        printer.append(" ").append(fixOperator.getOpIdentifier()).append(" ");
        currentSubTermIndex = 1;
        visitMaybeParen(application.getSubterm(1),
                fixOperator.getPrecedence() + 1);
    }

    /*
     * print an application in non-operator prefix form.
     */
    private void printApplication(Application application, String fctname)
            throws TermException {
        appendName(fctname);
        List<Term> subterms = application.getSubterms();
        if (subterms.size() > 0) {
            for (int i = 0; i < subterms.size(); i++) {
                printer.append(i == 0 ? "(" : ", ");
                currentSubTermIndex = i;
                subterms.get(i).visit(this);
            }
            printer.append(")");
        }
        if (pp.isTyped()) {
            printer.setStyle("type");
            printer.append(" as " + application.getType());
            printer.resetPreviousStyle();
        }
    }

    //
    // Visitors
    //
    
    public void visit(Variable variable) throws TermException {
        printer.setStyle("variable");
        TermTag oldTag = begin(variable);
        appendName(variable.getName());
        if(pp.isTyped()) {
            printer.setStyle("type");
            printer.append(" as " + variable.getType());
            printer.resetPreviousStyle(); 
        }
        printer.end();
        printer.resetPreviousStyle();
        currentTermTag = oldTag;
    }

    public void visit(Binding binding) throws TermException {
        TermTag oldTag = begin(binding);
        
        if(!printByPlugins(binding)) { 
            Binder binder = binding.getBinder();
            String bindname = binder.getName();
            printer.append("(").append(bindname).append(" ");
            printer.setStyle("variable");
            appendName(binding.getVariableName());
            printer.resetPreviousStyle();
            if (pp.isTyped()) {
                printer.setStyle("type");
                printer.append(" as ").append(binding.getVariableType().toString());
                printer.resetPreviousStyle();
            }
            int i = 0;
            for (Term t : binding.getSubterms()) {
                printer.append("; ");
                currentSubTermIndex = i++;
                t.visit(this);
            }
            printer.append(")");
        }
        printer.end();
        currentTermTag = oldTag;
    }
    
    public void visit(TypeVariableBinding typeVariableBinding) throws TermException {
        TermTag oldTag = begin(typeVariableBinding);
        
        String bindString = typeVariableBinding.getKind().image;
        String typevar = typeVariableBinding.getBoundType().toString();
        printer.append("(").append(bindString).append(" ").append(typevar).append("; ");
        currentSubTermIndex = 0;
        typeVariableBinding.getSubterm(0).visit(this);
        printer.append(")");
        printer.end();
        currentTermTag = oldTag;
    }
    
    public void visit(Application application) throws TermException {
        TermTag oldTag = begin(application);
        if(!printByPlugins(application)) { 
            boolean isInParens = inParens;
            inParens = false;
            if (isInParens)
                printer.append("(");

            Function function = application.getFunction();
            String fctname = function.getName();

            FixOperator fixOperator = null;
            if (pp.isPrintingFix())
                fixOperator = env.getReverseFixOperator(fctname);

            if (fixOperator != null) {
                if (pp.isTyped())
                    printer.append("(");

                if (function.getArity() == 1) {
                    printPrefix(application, fixOperator);
                } else {
                    printInfix(application, fixOperator);
                }

                if (pp.isTyped()) {
                    printer.append(")");
                    printer.setStyle("type");
                    printer.append(" as ")
                    .append(application.getType().toString());
                    printer.resetPreviousStyle();
                }

            } else {

                printApplication(application, fctname);

            }

            if (isInParens)
                printer.append(")");
        }
        printer.end();
        currentTermTag = oldTag;
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        TermTag oldTag = begin(schemaVariable);
        printer.append(schemaVariable.toString(pp.isTyped())).end();
        currentTermTag = oldTag;
    }
    
    public void visit(LiteralProgramTerm litProgTerm) throws TermException {
        TermTag oldTag = begin(litProgTerm);
        printer.setStyle("program");
        printer.append(litProgTerm.getModality().getOpeningDelimiter());
        printer.append(" " + Integer.toString(litProgTerm.getProgramIndex())
                + "; " + litProgTerm.getProgram() + " ");
        printer.append(litProgTerm.getModality().getClosingDelimiter());
        currentSubTermIndex = 0;
        visitMaybeParen(litProgTerm.getSuffixTerm(), Integer.MAX_VALUE);
        printer.resetPreviousStyle();
        printer.end();
        currentTermTag = oldTag;
    }
    
    public void visit(SchemaProgramTerm schemaProgramTerm)
            throws TermException {
        TermTag oldTag = begin(schemaProgramTerm);
        printer.setStyle("program");
        printer.append(schemaProgramTerm.getModality().getOpeningDelimiter()).append(" ");
        currentSubTermIndex = 0;
        schemaProgramTerm.getSchemaVariable().visit(this);
        if(schemaProgramTerm.hasMatchingStatement()) {
            printer.append(" : " + schemaProgramTerm.getMatchingStatement().toString(pp.isTyped()));
        }
        printer.append(" ").append(schemaProgramTerm.getModality().getClosingDelimiter());
        currentSubTermIndex = 0;
        schemaProgramTerm.getSuffixTerm().visit(this);
        printer.resetPreviousStyle();
        printer.end();
        currentTermTag = oldTag;
    }
    
    public void visit(UpdateTerm updateTerm) throws TermException {
        TermTag oldTag = begin(updateTerm);
        printer.setStyle("update");
        printer.append("{ ");
        
        List<Assignment> assignments = updateTerm.getAssignments();
        visit(assignments);

        printer.append(" }");
        printer.resetPreviousStyle();
        currentSubTermIndex = 0;
        visitMaybeParen(updateTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.end();
        currentTermTag = oldTag;
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


    public void visit(SchemaUpdateTerm schUpdateTerm) throws TermException {
        TermTag oldTag = begin(schUpdateTerm);
        printer.setStyle("update");
        printer.append("{ " + schUpdateTerm.getSchemaIdentifier() + " }");
        printer.resetPreviousStyle();
        currentSubTermIndex = 0;
        visitMaybeParen(schUpdateTerm.getSubterm(0), Integer.MAX_VALUE);
        printer.end();
        currentTermTag = oldTag;
    }


    public AnnotatedStringWithStyles<TermTag> getPrinter() {
        return printer;
    }

    /*
     * A statement is not a term, but we want to be able to print it also.
     */
    
    public void visitStatement(String keyword, Statement statement) throws TermException {
        printer.setStyle("statement");
        printer.setStyle("keyword");
        appendName(keyword);
        printer.resetPreviousStyle();
        printer.append(" ");
        
        List<Term> subterms = statement.getSubterms();
        for (int i = 0; i < subterms.size(); i++) {
            currentSubTermIndex = i;
            subterms.get(i).visit(this);
            if(i != subterms.size()-1)
                printer.append(", ");
        }
        printer.resetPreviousStyle();
    }
    
    public void visit(AssertStatement assertStatement)
            throws TermException {
        visitStatement("assert", assertStatement);
    }

    public void visit(AssignmentStatement assignmentStatement)
            throws TermException {
        printer.setStyle("statement");
        List<Assignment> assignments = assignmentStatement.getAssignments();
        visit(assignments);
        printer.resetPreviousStyle();
    }

    // used by AssignmentStatement, UpdateTerm and for text instantiation.
    public void visit(List<Assignment> assignments)
            throws TermException {
        
        for (int i = 0; i < assignments.size(); i++) {
            if(i > 0) {
                printer.append(" || ");
            }
            Assignment assignment = assignments.get(i);
            appendName(assignment.getTarget().toString(false));
            printer.append(" := ");
            currentSubTermIndex = i + 1;
            assignment.getValue().visit(this);
        }
    }
    
    public void visit(AssumeStatement assumeStatement)
            throws TermException {
        visitStatement("assume", assumeStatement);
    }

    public void visit(EndStatement endStatement) throws TermException {
        visitStatement("end", endStatement);        
    }

    public void visit(GotoStatement gotoStatement)
            throws TermException {
        visitStatement("goto", gotoStatement);        
    }

    public void visit(SkipStatement skipStatement)
            throws TermException {
        visitStatement("skip", skipStatement);        
    }

    public void visit(HavocStatement havocStatement)
            throws TermException {
        visitStatement("havoc", havocStatement);        
    }

    public void setCurrentSubTermIndex(int currentSubTermIndex) {
        this.currentSubTermIndex = currentSubTermIndex;
    }

}
