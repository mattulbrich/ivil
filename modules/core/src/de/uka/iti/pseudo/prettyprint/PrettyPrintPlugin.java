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

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;
import de.uka.iti.pseudo.util.Log;

/**
 * PrettyPrintPlugin is used to make the pretty printing of terms more flexible.
 * You can customize the way in which applications and bindings are printed.
 */
public abstract class PrettyPrintPlugin {
    
    private PrettyPrint prettyPrinter;
    private PrettyPrintVisitor prettyPrintVisitor;
    private AnnotatedStringWithStyles<TermTag> printer;

    final public synchronized boolean possiblyPrettyPrintTerm(Term term,
            PrettyPrintVisitor prettyPrintVisitor, PrettyPrint pp,
            AnnotatedStringWithStyles<TermTag> printer) {
        this.prettyPrinter = pp;
        this.prettyPrintVisitor = prettyPrintVisitor;
        this.printer = printer;
        
        int startLength = printer.length();
        
        try {
            if(term instanceof Application)
                prettyPrintTerm((Application) term);
            else if(term instanceof Binding)
                prettyPrintTerm((Binding) term);
            else
                throw new Error("term must be either Application or Binding");
            
        } catch (TermException e) {
            Log.log(Log.WARNING, "Error while prettyprinting, possibly messed output");
            Log.stacktrace(e);
        }
        
        return printer.length() > startLength;
    }
    
//    final public synchronized boolean possiblyPrettyPrintUpdate(Update update,
//            PrettyPrintVisitor prettyPrintVisitor, PrettyPrint pp,
//            AnnotatedStringWithStyles<TermTag> printer) throws TermException {
//        
//        this.prettyPrinter = pp;
//        this.prettyPrintVisitor = prettyPrintVisitor;
//        this.printer = printer;
//
//        int startLength = printer.length();
//        
//        for (AssignmentStatement assignment: update.getAssignments()) {
//            prettyPrintUpdate(assignment);    
//        }
//
//        return printer.length() > startLength;
//    }
    
    /**
     * Append arbitrary text to the output stream.
     * 
     * @param string
     */
    final protected void append(String string) {
        printer.append(string);
    }
    
    final protected void printSubterm(Term term, int subtermIndex) throws TermException {
        prettyPrintVisitor.setCurrentSubTermIndex(subtermIndex);
        Term subterm = term.getSubterm(subtermIndex);
        subterm.visit(prettyPrintVisitor);
    }
    
    protected void printBoundVariable(Binding binding) {
        printer.setStyle("variable");
        prettyPrintVisitor.appendName(binding.getVariableName());
        printer.resetPreviousStyle();
        if (prettyPrinter.isTyped()) {
            printer.setStyle("type");
            printer.append(" as ").append(binding.getVariableType().toString());
            printer.resetPreviousStyle();
        }
    }
    
    protected boolean isPrintingFix() {
        return prettyPrinter.isPrintingFix();
    }
    
    protected boolean isTyped() {
        return prettyPrinter.isTyped();
    }
    
    protected Environment getEnvironment() {
        return prettyPrinter.getEnvironment();
    }

    /**
     * Pretty print a term.
     * 
     * Implementing classes need to provide this method.
     * 
     * You can use the methods {@link #append(String)} and
     * {@link #printSubterm(Term)} to render the term.
     * 
     * @param term
     *            the term either a {@link Binding} or an {@link Application}.
     * @param visitor
     *            the visitor used for pretty printing
     * @param prettyPrint
     *            the pretty print the printer information
     * @param printer
     *            the actual print stream
     * 
     * @return true if the term has been rendered completely. false if nothing
     *         has been rendered. Nothing in between.
     * @throws TermException may be thrown by {@link #printSubterm(Term)} and {@link #append(String)}
     */
    public abstract void prettyPrintTerm(Application term) throws TermException;

    /**
     * Pretty print a term.
     * 
     * Implementing classes need to provide this method.
     * 
     * You can use the methods {@link #append(String)} and
     * {@link #printSubterm(Term)} to render the term.
     * 
     * @param term
     *            the term either a {@link Binding} or an {@link Application}.
     * @param visitor
     *            the visitor used for pretty printing
     * @param prettyPrint
     *            the pretty print the printer information
     * @param printer
     *            the actual print stream
     * 
     * @throws TermException
     *             may be thrown by {@link #printSubterm(Term)} and
     *             {@link #append(String)}
     * 
     * @return true if the term has been rendered completely. false if nothing
     *         has been rendered. Nothing in between.
     */
    public abstract void prettyPrintTerm(Binding term) throws TermException;

    /**
     * Find a replacement string for a name.
     * 
     * This method can be implemented, e.g., to shorten/change complex generated
     * names into more readable ones. A long name like
     * <code>var_x_def_on_line_12</code> could be abbreviated as <code>x</code>.
     * 
     * This method is called for:
     * <ul>
     * <li>function names,
     * <li>binder names,
     * <li>variable names
     * </ul>
     * in terms, statements and updates.
     * <p>
     * An implementation can return <code>null</code> to indicate that it has no
     * replacement for name. It may return name itself if it wishes to disallow
     * later plugins to come up with a replacement.
     * 
     * @param name
     *            the name to find a replacement for
     * 
     * @return a replacement for name, or null.
     */
    public abstract @Nullable String getReplacementName(@NonNull String name);

    
//    public abstract void prettyPrintUpdate(AssignmentStatement assignment) throws TermException;
    
}

