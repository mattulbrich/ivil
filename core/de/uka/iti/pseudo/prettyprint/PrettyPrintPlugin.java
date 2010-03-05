/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.prettyprint;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;

/**
 * PrettyPrintPlugin is used to make the pretty printing of terms more flexible.
 * You can customize the way in which applications and bindings are printed.
 * 
 * To implement a plugin you need to implement {@link #prettyPrintTerm(Term)}.
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
            System.err.println("Error while prettyprinting, possibly messed output");
            e.printStackTrace();
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
        printer.append(binding.getVariableName());
        if (prettyPrinter.isTyped())
            printer.append(" as ").append(binding.getType().toString());
        printer.resetPreviousStyle();
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
     *            @throws TermException may be thrown by {@link #printSubterm(Term)} and {@link #append(String)}
     * 
     * @return true if the term has been rendered completely. false if nothing
     *         has been rendered. Nothing in between.
     */
    public abstract void prettyPrintTerm(Binding term) throws TermException;

    
//    public abstract void prettyPrintUpdate(AssignmentStatement assignment) throws TermException;
    
}

