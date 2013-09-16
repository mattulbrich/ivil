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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.prettyprint.AnnotatedString.Style;
import de.uka.iti.pseudo.prettyprint.PrettyPrintLayouter;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

/**
 * This class is the entry point to the pretty priting system of ivil. There are
 * static and dynamic entry points to this class.
 *
 * <pre>
 * PrettyPrint.print(env, term)
 * </pre>
 *
 * can be used to write a term sporadically. Programs, statments, rules can be
 * pretty printed as well.
 *
 * An {@link AnnotatedString} is returned by the methods, possibly with linebreaks;
 *
 * @see AnnotatedString
 * @author mattze
 */
public class PrettyPrint {

    public static final String TYPED_PROPERTY = "pseudo.pp.typed";
    public static final String PRINT_FIX_PROPERTY = "pseudo.pp.printFix";
    public static final String PRINT_PLUGINS_PROPERTY = "pseudo.pp.printPlugins";
    public static final String INITIALSTYLE_PROPERTY = "pseudo.pp.initialstyle";
    public static final String BREAK_MODALITIES_PROPERTY = "pseudo.pp.breakModalities";
    public static final String SERVICE_NAME = "prettyPrinter";

    /**
     * The environment to lookup infix and prefix operators, plugins
     */
    private final Environment env;

    /**
     * the list of installed pretty print plugins (extracted from env)
     */
    private List<PrettyPrintPlugin> pluginPrettyPrinters;

    /**
     * whether or not types are to be printed.
     * (Defaults to default in constructor
     */
    private boolean typed;

    /**
     * whether or not fix operators are printed as such.
     */
    private boolean printFix = true;

    /**
     * whether or not the pretty printer plugins are to be used.
     */
    private boolean printPlugins = true;

    /**
     * the style (attribute string) to be set in the beginning.
     * may be null if no special attributes are to be set.
     */
    private Style initialStyle;

    /**
     * create a new pretty printer with the default properties preset.
     *
     * @param env
     *            the environment to lookup infix and prefix operators
     */
    public PrettyPrint(@NonNull Environment env) {
        this(env, false);
    }

    /**
     * create a new pretty printer with the typing property explicitly set.
     *
     * @param typed
     *            whether or not typing information is to be printed.
     * @param env
     *            the environment to lookup infix and prefix operators
     */
    public PrettyPrint(@NonNull Environment env, boolean typed) {
        this.env = env;
        this.typed = typed;

        try {
            List<PrettyPrintPlugin> list = env.getPluginManager().getPlugins(
                    SERVICE_NAME, PrettyPrintPlugin.class);
            this.pluginPrettyPrinters = Collections.unmodifiableList(list);
        } catch (EnvironmentException e) {
            e.printStackTrace();
            Log.log(Log.WARNING, "Disabling pretty printer plugins!");
            this.pluginPrettyPrinters = Collections.emptyList();
        }
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
    public AnnotatedString print(Term term) {
        return print(term, Integer.MAX_VALUE);
    }

    /**
     * pretty print a term using the currently set properties on this object.
     *
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     *
     * @param term
     *            the term to pretty print
     *
     * @param linewidth
     *            length of a line, a positive number
     *
     * @return a freshly created annotated string object
     */
    public AnnotatedString print(Term term, int linewidth) {
        return print(term, new PrettyPrintLayouter(linewidth));
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
     *            the annotated string to append the term to
     * @return printer
     */
    private AnnotatedString print(Term term, PrettyPrintLayouter printer) {
        PrettyPrintVisitor visitor = new PrettyPrintVisitor(this, printer);

        try {
            if(initialStyle != null) {
                printer.setStyle(initialStyle);
            }

            term.visit(visitor);

            if(initialStyle != null) {
                printer.resetPreviousStyle();
            }

            if(!printer.hasEmptyStack()) {
                throw new TermException("Cannot render: " + term);
            }

            return printer.getAnnotatedString();
        } catch (TermException e) {
            // not thrown in this code
            Log.log(Log.WARNING, "A term cannot be pretty-printed: %s", term);
            Log.stacktrace(Log.WARNING, e);
            AnnotatedString result = new AnnotatedString(0);
            result.print(term.toString(true));
            return result;
        }
    }

    /** TODO DOC
     * pretty print a term using the currently set properties on this object.
     *
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     *
     * @param statement
     *            the term to pretty print
     * @return a freshly created annotated string object
     */
    public AnnotatedString print(Statement statement) {
        return print(statement, new PrettyPrintLayouter());
    }

    /** TODO DOC */
    private AnnotatedString print(Statement statement,
            PrettyPrintLayouter printer) {

        PrettyPrintVisitor visitor = new PrettyPrintVisitor(this, printer);
        try {
            if(initialStyle != null) {
                printer.setStyle(initialStyle);
            }

            statement.accept(visitor);

            if(initialStyle != null) {
                printer.resetPreviousStyle();
            }
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }

        assert printer.hasEmptyStack() : "The annotation stack has not been emptied: " + statement;

        return printer.getAnnotatedString();
    }

    /**
     * pretty print a list of assignments using the currently set properties on
     * this object. The format is as follows:
     *
     * <pre>
     *  v1 := exp1 || v2 := exp2 || ... || vn := expn
     * </pre>
     *
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     *
     * @param assignments
     *            the list to pretty print
     * @return a freshly created annotated string object.
     */
    public AnnotatedString print(@DeepNonNull List<Assignment> assignments) {
        return print(assignments, new PrettyPrintLayouter());
    }

    /**
     * pretty print a list of assignments using the currently set properties on
     * this object. The format is as follows:
     * <pre>
     *  v1 := exp1 || v2 := exp2 || ... || vn := expn
     * </pre>
     *
     * The result is an annotated String in which to every character the
     * innermost containing subterm can be obtained.
     *
     * @param assignmentList
     *            the list to pretty print
     * @param printer
     *            the annotated string to append the term to
     * @return the argument printer
     */
    public AnnotatedString print(List<Assignment> assignmentList,
            PrettyPrintLayouter printer) {

        PrettyPrintVisitor visitor = new PrettyPrintVisitor(this, printer);
        try {
            if(initialStyle != null) {
                printer.setStyle(initialStyle);
            }

            visitor.visit(assignmentList);

            if(initialStyle != null) {
                printer.resetPreviousStyle();
            }

        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }
        return printer.getAnnotatedString();
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
    public static @NonNull AnnotatedString print(
            @NonNull Environment env, @NonNull Term term) {
        return print(env, term, false);
    }

    /**
     * Prints a term.
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
     *
     * @return the annotated string
     */
    public static @NonNull AnnotatedString print(
            @NonNull Environment env, @NonNull Term term, boolean typed) {
        PrettyPrint pp = new PrettyPrint(env, typed);

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
    public static @NonNull AnnotatedString print(
            @NonNull Environment env, @NonNull LocatedTerm lterm) {
        return print(env, lterm, false);
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
     *
     * @return an annotated string
     */
    public static @NonNull AnnotatedString print(
            @NonNull Environment env, @NonNull LocatedTerm lterm,
            boolean typed) {
        PrettyPrint pp = new PrettyPrint(env, typed);
        PrettyPrintLayouter retval;
        retval = new PrettyPrintLayouter();

        switch (lterm.getMatchingLocation()) {
        case ANTECEDENT:
            pp.print(lterm.getTerm(), retval);
            retval.append(" |-");
            break;

        case SUCCEDENT:
            retval.append("|- ");
            pp.print(lterm.getTerm(), retval);
            break;

        case BOTH:
            pp.print(lterm.getTerm(), retval);
            break;
        }

        return retval.getAnnotatedString();
    }

    /**
     * Pretty print the rule using the term pretty printer into a string
     *
     * @param env
     *            the environment to lookup infix etc.
     *
     * @return the rule as pretty printed string.
     */
    public String print(Rule rule) {
        StringBuilder sb = new StringBuilder();
        sb.append("rule ").append(rule.getName()).append("\n");

        LocatedTerm findClause = rule.getFindClause();
        if(findClause != null) {
            sb.append("  find ").append(PrettyPrint.print(env, findClause)).append("\n");
        }

        for (LocatedTerm ass : rule.getAssumptions()) {
            sb.append("  assume ").append(PrettyPrint.print(env, ass)).append("\n");
        }
        for (WhereClause where : rule.getWhereClauses()) {
            sb.append("  where ");
            if(where.isInverted()) {
                sb.append("not ");
            }
            sb.append(where.getWhereCondition().getName());
            for (Term arg : where.getArguments()) {
                sb.append(" ").append(PrettyPrint.print(env, arg));
            }
            sb.append("\n");
        }

        for (GoalAction action : rule.getGoalActions()) {
            switch(action.getKind()) {
            case CLOSE: sb.append("  closegoal"); break;
            case COPY: sb.append("  samegoal"); break;
            case NEW: sb.append("  newgoal"); break;
            }

            sb.append("\n");
            Term rep = action.getReplaceWith();
            if(rep != null) {
                sb.append("    replace ").append(PrettyPrint.print(env, rep)).append("\n");
            } else if(action.isRemoveOriginalTerm()) {
                sb.append("    remove\n");
            }
            for (Term t : action.getAddAntecedent()) {
                sb.append("    add ").append(PrettyPrint.print(env, t)).append(" |-").append("\n");
            }
            for (Term t : action.getAddSuccedent()) {
                sb.append("    add |-").append(PrettyPrint.print(env, t)).append("\n");
            }
        }

        Collection<String> definedProperties = rule.getDefinedProperties();
        if(!definedProperties.isEmpty()) {
            sb.append("    tags\n");
        }
        for (String property : definedProperties) {
            sb.append("      ").append(property).append(" ")
                    .append(Util.addQuotes(rule.getProperty(property))).append("\n");
        }
        return sb.toString();
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
     * Checks if the printer is typed.
     *
     * @return true, if is typed
     */
    public boolean isTyped() {
        return typed;
    }

    /**
     * Checks if the printer is printing infix and prefix.
     *
     * @return true, if is printing fix
     */
    public boolean isPrintingFix() {
        return printFix;
    }

    /**
     * Checks if is printing using the pretty printer plugins.
     *
     * @return true, if is printing fix
     */
    public boolean isPrintingPlugins() {
        return printPlugins;
    }

    /**
     * retrieve all registered pretty printer plugins.
     * @return an unmodifiable list
     */
    public List<PrettyPrintPlugin> getPrettyPrinterPlugins() {
        return pluginPrettyPrinters;
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
     * Sets the printing plugins.
     *
     * All registered {@link PropertyChangeListener} are informed of this
     * change if the current value differs from the set value.
     *
     * @param selected
     *            the new printing plugins value
     */
    public void setPrintingPlugins(boolean printPlugins) {
        boolean old = this.printPlugins;
        this.printPlugins = printPlugins;
        firePropertyChanged(PRINT_PLUGINS_PROPERTY, old, printPlugins);
    }


    /**
     * The system to manage properties
     */
    private PropertyChangeSupport propertiesSupport;


    /**
     * get the style (attribute string) that is to be set in the beginning
     *
     * @return a string or possibly null
     */
    public @Nullable Style getInitialStyle() {
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
    public void setInitialStyle(@Nullable Style initialStyle) {
        Style old = this.initialStyle;
        this.initialStyle = initialStyle;
        firePropertyChanged(INITIALSTYLE_PROPERTY, old, initialStyle);
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
     * @param listener
     *            the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(propertiesSupport == null) {
            propertiesSupport = new PropertyChangeSupport(this);
        }
        propertiesSupport.addPropertyChangeListener(listener);
    }

    /**
     * Adds a property change listener for a particular property.
     *
     * @param property
     *            the property
     * @param listener
     *            the listener
     */
    public void addPropertyChangeListener(String property,
            PropertyChangeListener listener) {
        if(propertiesSupport == null) {
            propertiesSupport = new PropertyChangeSupport(this);
        }
        propertiesSupport.addPropertyChangeListener(property, listener);
    }

    /**
     * Removes the property change listener.
     *
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(propertiesSupport != null) {
            propertiesSupport.removePropertyChangeListener(listener);
        }
    }

    /**
     * Fire property changed.
     *
     * @param property
     *            the property
     * @param oldVal
     *            the old value
     * @param newVal
     *            the new value
     */
    private <E> void firePropertyChanged(String property, E oldVal, E newVal) {
        if(propertiesSupport != null && !Util.equalOrNull(oldVal, newVal)) {
            propertiesSupport.firePropertyChange(property, oldVal, newVal);
        }
    }

}
