/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class SMTLibTranslator translates a term / formula / sequent to its
 * corresponding SMTLib counterpart.
 * 
 * <p>
 * The translation assumes that certain names have their intended meaning. For
 * instance, <code>$add</code> should be the addition of integers as defined by
 * the rules in <code>$int.p</code>.
 * 
 * <p>
 * Ivil does not distinguish between boolean terms and formulas. The translation
 * has to, however. Therefore, a mechnism is used which lazily translates a
 * formula to a term (or vice versa) only if needed.
 * 
 * <p>
 * <a href="http://goedel.cs.uiowa.edu/smtlib/">Page of SMT-LIB</a>
 * 
 * @author mattias ulbrich
 */
public class SMTLibTranslator extends DefaultTermVisitor {
    
    /**
     * Mapping of built-in functions to built-in smtlib function symbols.
     */
    private static final String[] BUILTIN_FUNCTIONS = {
        "false", "false",
        "true", "true",
        "$not", "not",
        "$and", "and",
        "$or", "or",
        "$impl", "implies",
        "$equiv", "iff",
        "\\forall", "forall",
        "\\exists", "exists",
        "$gt", ">",
        "$lt", "<",
        "$gte", ">=",
        "$lte", "<=",
        "$eq", "=",
        "$plus", "+",
        "$minus", "-",
        "$mult", "*",
        "$neg", "-"
    };
    
    /**
     * These symbols are predicates and, hence, result in a FORMULA rather than
     * in a TERM.
     */
    private static final List<String> PREDICATES = Util.readOnlyArrayList(new String[] {
        "true", "false", "and", "or", "implies", "iff", "not", "<", ">", "<=", ">=", "="
    });

    /**
     * map storing how function symbols are mapped to SMT counterparts.
     */
    private Map<String,String> translationMap = new HashMap<String, String>();
    
    /**
     * counter used to create new distinct symbols (its increment on creation)
     */
    private int unknownCounter = 0;
    
    /*
     * these storages can be read by test cases, therefore package readable
     */
    /**
     * a set of definitions of user created sorts.
     */
    /* package */ Set<String> extrasorts = new HashSet<String>();
    
    /**
     * a set of definitions of user created function symbols.
     */
    /* package */ Set<String> extrafuncs = new HashSet<String>();
    private String result;

    
    /**
     * A constant denoting that the current resulting entity is a FORMULA.
     * @see #TERM
     */
    private static boolean FORMULA = true;
    
    /**
     * A constant denoting that the current resulting entity is a TERM.
     * @see #FORMULA
     */
    private static boolean TERM = false;
    
    /**
     * The type (formula or term) of the currently hold result.
     * @see #FORMULA
     * @see #TERM
     */
    private boolean resultType;

    /**
     * the "cond" function from the environment must be
     * treated separately. This may be null if the function is not defined.
     */
    private Function condFunction;
    
    /**
     * All axioms as they are extracted from the environment
     */
    private Collection<Axiom> allAxioms;
    
    
    /**
     * Instantiates a new SMT-lib translator.
     * 
     * @param env
     *            the environment to use.
     */
    public SMTLibTranslator(Environment env) {
        for (int i = 0; i < BUILTIN_FUNCTIONS.length; i += 2) {
            translationMap.put(BUILTIN_FUNCTIONS[i], BUILTIN_FUNCTIONS[i+1]);
        }
        
        condFunction = env.getFunction("cond");
        allAxioms = env.getAllAxioms();
    }

    /**
     * Translate a term to its smt-lib counterpart.
     * 
     * <p>
     * Possibly, sort and constant definitions are written to the apropriate
     * storages.
     * 
     * @param term
     *            the term to translate
     * 
     * @return the string representing the translation
     * 
     * @throws TermException
     *             if the translation fails for whatever reason
     */
    public String translate(Term term) throws TermException {
        term.visit(this);
        return result;
    }

    /**
     * Translate a sequent to its smt-lib counterpart, i.e., a formula.
     * 
     * <p>
     * Possibly, sort and constant definitions are written to the apropriate
     * storages.
     * 
     * <p>
     * The resulting formula is a conjunction of all formulas on the sequent's
     * lhs (positive) and all formulas of the rhs (negative).
     * For the empty sequent <code>"(and true)"</code> is returned.
     * 
     * @param sequent
     * the sequent to translate
     * 
     * @return the string representing the translation
     * 
     * @throws TermException
     *             if the translation fails for whatever reason
     */
    public String translate(Sequent sequent) throws TermException {
        StringBuilder sb = new StringBuilder();
        sb.append("(and true "); 
        for (Term term : sequent.getAntecedent()) {
            term.visit(this);
            sb.append(resultAs(FORMULA));
        }
        for (Term term : sequent.getSuccedent()) {
            sb.append("(not ");
            term.visit(this);
            sb.append(resultAs(FORMULA));
            sb.append(")");
        }
            
        sb.append(")");
        String result = sb.toString();
        return result;
    }

    /**
     * Export a sequent to an output stream.
     * 
     * <p>
     * It will translate the sequent, add all axioms from the environment and
     * write everything to the output stream. If new sorts and/or symbols are
     * created, they get declared, too.
     * 
     * @param sequent
     *            the sequent to export
     * 
     * @param builder
     *            the stream to output to
     * 
     * @throws TermException
     *             if the translation fails
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void export(@NonNull Sequent sequent, @NonNull Appendable builder)
            throws TermException, IOException {

        extrafuncs.clear();
        extrasorts.clear();
        
        List<String> axioms = translateAxioms();
        
        String translation = translate(sequent);
        
        builder.append("; created by ivil " + new Date());
        builder.append("\n(benchmark ivil_verification\n");
        builder.append(":logic AUFLIA\n\n");
        includePreamble(builder);
        builder.append(":extrasorts (" + Util.join(extrasorts, "\n   ") + ")\n\n");
        builder.append(":extrafuns (" + Util.join(extrafuncs, "\n   ") + ")\n\n");
        for (String ax : axioms) {
            builder.append(":assumption (" + indent(ax) + ")\n");
        }
        builder.append(":assumption (" + indent(translation) + ")\n)");
    }

    private void includePreamble(Appendable pw) throws IOException {
        InputStream stream = getClass().getResourceAsStream("preamble.smt");
        if(stream == null)
            throw new IOException("Resource preamble.smt not found");
        Reader r = new InputStreamReader(stream);
        char[] buffer = new char[1024];
        int read = r.read(buffer);
        while(read != -1) {
            pw.append(new String(buffer, 0, read));
            read = r.read(buffer);
        }
    }


    /**
     * translate all axioms to strings and return the list of strings.
     */
    private List<String> translateAxioms() throws TermException {
        List<String> ret = new ArrayList<String>();
        for (Axiom ax : allAxioms) {
            Term term = ax.getTerm();
            term.visit(this);
            ret.add(resultAs(FORMULA));
        } 
        return ret;
    }

    /*
     * by default replace by a new unknown symbol.
     * 
     * TODO cache replacement results => use same symbol for identical terms
     */
    protected void defaultVisitTerm(Term term) throws TermException {
        String name = "unknown" + unknownCounter;
        unknownCounter++;
        result = name;
        resultType = TERM;
        
        extrafuncs.add("(" + name + " " + makeSort(term.getType()) + ")");
    }
    
    public void visit(Application application) throws TermException {
        Function function = application.getFunction();
        String name = function.getName();
        String translation = translationMap.get(name);
        
        if(function == condFunction) {
            StringBuilder sb = new StringBuilder();
            sb.append("(ite ");
            application.getSubterm(0).visit(this);
            sb.append(resultAs(FORMULA)).append(" ");
            application.getSubterm(1).visit(this);
            sb.append(resultAs(TERM)).append(" ");
            application.getSubterm(2).visit(this);
            sb.append(resultAs(TERM)).append(")");
            result = sb.toString();
            return;
        }
        
        if(translation == null && function instanceof NumberLiteral) {
            translation = function.getName();
            translationMap.put(translation, translation);
        }

        if(translation == null) {
            translation = makeExtraFunc(application);
        }

        boolean hasArgs = application.countSubterms() > 0;

        if(hasArgs) {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(translation);
            for (Term subterm : application.getSubterms()) {
                sb.append(" ");
                subterm.visit(this);
                boolean expectedType = PREDICATES.contains(translation) 
                    && subterm.getType().equals(Environment.getBoolType()) ? FORMULA : TERM;
                sb.append(resultAs(expectedType));
            }
            sb.append(")");
            result = sb.toString();
        } else {
            result = translation;
        }
        resultType = PREDICATES.contains(translation) ? FORMULA : TERM;
    }
    
    public void visit(Binding binding) throws TermException {
        Binder binder = binding.getBinder();
        String name = binder.getName();
        String translation = translationMap.get(name);
        
        if(translation == null) {
            defaultVisitTerm(binding);
            return;
        }
        
        StringBuilder retval = new StringBuilder("(" + translation);
        BindableIdentifier variable = binding.getVariable();
        
        assert variable instanceof Variable;
        
        String bound = variable.toString(false);
        String boundType = makeSort(variable.getType());

        retval.append(" (?").append(bound).append(" ").
                append(boundType).append(") ");
        
        binding.getSubterm(0).visit(this);
        retval.append(resultAs(FORMULA)).append(")");
        
        result = retval.toString();
        resultType = FORMULA;
    }
    
    private String resultAs(boolean type) {
        if(type == resultType)
            return result;
        
        if(type == FORMULA) {
            // from term to formula
            return "(= " + result + " termTrue" + ")";
        } else {
            // from formula to term
            return "(ite " + result + " termTrue termFalse)";
        }
    }

    public void visit(Variable variable) throws TermException {
        result = "?" + variable.getName();
        resultType = TERM;
    }

    private String makeExtraFunc(Application application) {
        String name = "extra." + application.getFunction().getName();
        name = name.replace('$', '_');
        
        String resultType = makeSort(application.getType());
        
        List<Term> subterms = application.getSubterms();
        String[] argTypes = new String[subterms.size()];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = makeSort(subterms.get(i).getType());
        }
        
        String result = name + "." + Util.join(argTypes, ".") + "." + resultType;
        
        extrafuncs.add("(" + result + " " + Util.join(argTypes, " ") + " " + resultType + ")");
        
        return result;
    }
    
    private String makeSort(Type type) {
        String typeString = toString(type);
        if(typeString.equals("int"))  {
            return "Int";
        } else if(typeString.equals("bool"))  {
            return "Bool";
        } else {
            extrasorts.add(typeString);
            return typeString;
        }
    }

    private String toString(Type t) {
        return t.toString().replaceAll("[\\(\\),]", "_");
    }
        
    
    @SuppressWarnings("fallthrough")
    public static String indent(String string) {

        StringBuilder sb = new StringBuilder();
        int indention = 0;
        
        for(int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch(c) {
            case '(':
                sb.append("\n");
                for(int j = 0; j < indention; j++)
                    sb.append(" ");
                sb.append("(");
                indention++;
                break;
            case ')':
                indention--;
                // fall through
            default:
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
}
