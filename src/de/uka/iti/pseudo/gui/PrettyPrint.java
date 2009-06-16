/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;
import de.uka.iti.pseudo.util.Util;

// TODO: Auto-generated Javadoc


public class PrettyPrint {

    public static final String TYPED_PROPERTY = "PP.typed";
    public static final String PRINT_FIX_PROPERTY = "PP.printFix";
    public static final String INITIALSTYLE_PROPERTY = "PP.initialstyle";
    public static final String BREAK_MODALITIES_PROPERTY = "PP.breakModalities";

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
    
    /**
     * whether or not program modifications are printed verbosely.
     */
    private boolean printProgramModifications;
    
    /**
     * whether or not to break lines in modalities and indent 
     */
    private boolean breakModalities = Boolean.getBoolean("pseudo.breakModalities");

    /**
     * the style (attribute string) to be set in the beginning.
     * may be null if no special attributes are to be set.
     */
    private String initialStyle;

    /**
     * create a new pretty printer with some properties preset.
     * 
     * @param env
     *            the environment to lookup infix and prefix operators
     * @param typed
     *            terms are printed with their types if set to true
     * @param printFix
     *            fix terms are printed as infix/prefix if true, otherwise as
     *            functions.
     */
    public PrettyPrint(@NonNull Environment env, boolean typed,
            boolean printFix) {
        this.env = env;
        this.typed = typed;
        this.printFix = printFix;
    }
    
    /**
     * pretty print a term using the currently set properties on this object.
     * 
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     * 
     * @param term
     *            the term to pretty print
     * @return a freshly created annotated string object
     */
    public AnnotatedStringWithStyles<Term> print(Term term) {
        return print(term, new AnnotatedStringWithStyles<Term>());
    }
    
    /**
     * pretty print a term using the currently set properties on this object.
     * 
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     * 
     * @param term
     *            the term to pretty print
     * @param printer
     *            the annotated string to appent the term to
     * @return printer
     */
    public AnnotatedStringWithStyles<Term> print(Term term, AnnotatedStringWithStyles<Term> printer) {
        PrettyPrintVisitor visitor = new PrettyPrintVisitor(this, printer);
        
        try {
            if(initialStyle != null)
                printer.setStyle(initialStyle);
            
            term.visit(visitor);
            
            if(initialStyle != null)
                printer.resetPreviousStyle();
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }

        assert printer.hasEmptyStack();
        
        return printer;
    }

    
    /**
     * Prints a term without explicit typing, but fix operators in prefix or
     * infix notation.
     * 
     * A temporary pretty printer is created for printing.
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
    public static @NonNull AnnotatedStringWithStyles<Term> print(
            @NonNull Environment env, @NonNull Term term) {
        return print(env, term, false, true);
    }

    /**
     * Prints a term without explicit typing.
     * 
     * A temporary pretty printer object is created for printing.
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
    public static @NonNull AnnotatedStringWithStyles<Term> print(
            @NonNull Environment env, @NonNull Term term, boolean typed,
            boolean printFix) {
        PrettyPrint pp = new PrettyPrint(env, typed, printFix);

        return pp.print(term);        
    }

    /**
     * Prints a located term without explicit typing.
     * 
     * A temporary pretty printer is created for printing.
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
    public static @NonNull AnnotatedStringWithStyles<Term> print(
            @NonNull Environment env, @NonNull LocatedTerm lterm) {
        return print(env, lterm, false, true);
    }
    
    /**
     * Prints a located term.
     * 
     * A temporary pretty printer is created for printing.
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
    public static @NonNull AnnotatedStringWithStyles<Term> print(
            @NonNull Environment env, @NonNull LocatedTerm lterm,
            boolean typed, boolean printFix) {
        PrettyPrint pp = new PrettyPrint(env, typed, printFix);
        AnnotatedStringWithStyles<Term> retval;

        switch (lterm.getMatchingLocation()) {
        case ANTECEDENT:
            retval = pp.print(lterm.getTerm());
            retval.append(" |-");
            assert retval.hasEmptyStack();
            return retval;

        case SUCCEDENT:
            retval = new AnnotatedStringWithStyles<Term>();
            retval.append("|- ");
            pp.print(lterm.getTerm(), retval);
            assert retval.hasEmptyStack();
            return retval;

        case BOTH:
            retval = pp.print(lterm.getTerm());
            assert retval.hasEmptyStack();
            return retval;
        }
        // unreachable
        throw new Error();
    }

    /**
     * Sets the typed.
     * 
     * All registered {@link PropertyChangeListener} are informed of this
     * change if the current value differs from the set value.
     * 
     * @param typed the new typed
     */
    public void setTyped(boolean typed) {
        boolean old = this.typed;
        this.typed = typed;
        firePropertyChanged(TYPED_PROPERTY, old, typed);
    }        


    /**
     * Checks if is typed.
     * 
     * @return true, if is typed
     */
    public boolean isTyped() {
        return typed;
    }

    /**
     * Checks if is printing fix.
     * 
     * @return true, if is printing fix
     */
    public boolean isPrintingFix() {
        return printFix;
    }
    
    /**
     * Sets the printing fix.
     * 
     * All registered {@link PropertyChangeListener} are informed of this
     * change if the current value differs from the set value.
     * 
     * @param printFix the new printing fix
     */
    public void setPrintingFix(boolean printFix) {
        boolean old = this.printFix;
        this.printFix = printFix;
        firePropertyChanged(PRINT_FIX_PROPERTY, old, printFix);
    }

    /**
     * The underlying annotating string
     */
    //private AnnotatedStringWithStyles<Term> printer;

    private PropertyChangeSupport propertiesSupport;


    /**
     * get the style (attribute string) that is to be set in the beginning
     * 
     * @return a string or possibly null 
     */
    public @Nullable String getInitialStyle() {
        return initialStyle;
    }

    /**
     * set the style (attribute string) that is to be set in the beginning.
     * 
     * All registered {@link PropertyChangeListener} are informed of this
     * change if the current value differs from the set value.
     * 
     * @param initialStyle
     *   the style to be set at top level, or null if none is to be set
     */
    public void setInitialStyle(@Nullable String initialStyle) {
        String old = this.initialStyle;
        this.initialStyle = initialStyle;
        firePropertyChanged(INITIALSTYLE_PROPERTY, old, initialStyle);
    }
    
    /**
     * Checks if is this printer indents in modalities and breaks lines in modalities
     * 
     * @return true, if is this breaks lines and indents modalities
     */
    public boolean isBreakModalities() {
        return breakModalities;
    }

    /**
     * Sets if is this printer should indent in modalities and break lines in
     * modalities.
     * 
     * All registered {@link PropertyChangeListener} are informed of this
     * change if the current value differs from the set value.
     * 
     * @param breakModalities
     *            if true, this breaks lines and indents modalities from now on.
     */
    public void setBreakModalities(boolean breakModalities) {
        boolean old = this.breakModalities;
        this.breakModalities = breakModalities;
        firePropertyChanged(BREAK_MODALITIES_PROPERTY, old, breakModalities);
    }
    
    
    // TODO DOC
    public boolean isPrintingProgramModifications() {
        return printProgramModifications;
    }

    public void setPrintingProgramModifications(boolean printProgramModifications) {
        this.printProgramModifications = printProgramModifications;
    }


    /**
     * get the environment upon which the pretty printer relies.
     * 
     * @return the underlying environment
     */
    public Environment getEnvironment() {
        return env;
    }
    
    /**
     * Adds the property change listener.
     * 
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(propertiesSupport == null)
            propertiesSupport = new PropertyChangeSupport(this);
        propertiesSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes the property change listener.
     * 
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(propertiesSupport != null)
            propertiesSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fire property changed.
     * 
     * @param property the property
     * @param oldVal the old val
     * @param newVal the new val
     */
    private <E> void firePropertyChanged(String property, E oldVal, E newVal) {
        if(propertiesSupport != null && !Util.equalOrNull(oldVal, newVal))
            propertiesSupport.firePropertyChange(property, oldVal, newVal);
    }

   
    
}
