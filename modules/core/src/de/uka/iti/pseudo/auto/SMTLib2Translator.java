/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.BOOL;
import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.INT;
import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.UNIVERSE;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;


import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.rule.where.NoFreeVars;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.creation.TypeMatchVisitor;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;
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
 * has to, however. Therefore, a mechanism is used which lazily translates a
 * formula to a term (or vice versa) only if needed.
 *
 * <p>
 * Since the type system of ivil is by far more complex than the simple type
 * system of smt, we map most of the types to one type "Universe" and introduce
 * type predicates. Hence, type quantification becomes regular quantification
 * and for every type a function symbol is introduced. For every constant symbol
 * a typing axiom is assumed.
 *
 * <p>
 * Integer treatment is (as far as possible) kept apart from the universe
 * treatment.
 *
 * <p>
 * <a href="http://goedel.cs.uiowa.edu/smtlib/">Page of SMT-LIB</a>
 *
 * @author mattias ulbrich
 */
public class SMTLib2Translator extends DefaultTermVisitor implements SMTLibTranslator {

    /**
     * This prorperty key prefix can be used to define SMT2 inline code in .p
     * files.
     */
    private static final String INLINE_DIRECTIVE = "smt2.inline.";

    /**
     * Mapping of built-in functions to built-in smtlib function symbols.
     */
    private static final String[] BUILTIN_FUNCTIONS = { "false", "false",
        "true", "true", "$not", "not", "$and", "and", "$or", "or", "$impl",
        "implies", "$equiv", "iff", "\\forall", "forall", "\\exists",
        "exists", "$gt", ">", "$lt", "<", "$gte", ">=", "$lte", "<=",
        "$plus", "+", "$minus", "-", "$mult", "*", "$div",
        "div", "$mod", "mod", "$neg", "-", "\\T_all", "forall",
        "\\T_exists", "exists" };

    /**
     * These symbols are special quantifiers which are treated separately.
     */
    private static final List<String> QUANTIFIERS = Util
            .readOnlyArrayList(new String[] { "\\forall", "\\exists",
                    "\\T_all", "\\T_exists" });

    /**
     * These symbols are predicates and, hence, result in a FORMULA rather than
     * in a TERM.
     */
    private static final List<String> PROPOSITIONAL_PREDICATES = Util
            .readOnlyArrayList(new String[] { "and", "or", "implies", "iff",
            "not" });

    private static final List<String> ALL_PREDICATES = Util
            .readOnlyArrayList(new String[] { "true", "false", "and", "or",
                    "implies", "iff", "not", "<", ">", "<=", ">=", "=" });

    private static final Comparator<Object> STRING_COMPARATOR =
            new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    /**
     * map storing how function symbols are mapped to SMT counterparts.
     */
    private final Map<String, String> translationMap = new HashMap<String, String>();

    /**
     * map storing the type variables which appear only in result types of
     * function symbols.
     */
    //    private Map<String, Set<TypeVariable>> freeTypeVarMap = new HashMap<String, Set<TypeVariable>>();

    /**
     * counter used to create new distinct symbols (its increment on creation)
     */
    private int unknownCounter = 0;

    /**
     * counter used to let every unique function symbol point to something different.
     */
    private int uniqueCounter;

    /**
     * a set of definitions of user created function symbols.
     */
    /* package */Set<String> extrafuncs = new LinkedHashSet<String>();

    /**
     * a set of assumptions that are due to the translation.
     *
     * If the content contains more than one line, everything but the last line
     * is taken to be comment.
     */
    /* package */LinkedList<String> assumptions = new LinkedList<String>();

    /**
     * Strings treated as smt expressions can be of these three kinds.
     */
    public static enum ExpressionType {
        BOOL("Bool"), INT("Int"), UNIVERSE("Universe");

        private String img;

        ExpressionType(String img) {
            this.img = img;
        }

        @Override
        public String toString() {
            return img;
        }
    }

    /**
     * Used by the visit functions as arguments. The visitation has to deposit
     * the result of the translation in the variable {@link #result} and it has
     * to be of the type {@link #requestedType}.
     */
    private ExpressionType requestedType = BOOL;

    /**
     * Used by the visit functions to pass on the expression type of a
     * translation. The string in {@link #result} has to be of this type.
     */
    private ExpressionType resultingType = BOOL;

    /**
     * Used by the visit functions to pass on the result of a translation. The
     * string placed here has to be of type {@link #resultingType}.
     */
    private String result = "";

    /**
     * The stack of quantified smt-variables. Used by {@link #typeToTerm},
     * updated by {@link #visit(Binding)} and
     * {@link #visit(TypeVariableBinding)}
     */
    private final Deque<String> quantifiedVariables = new LinkedList<String>();

    /**
     * the "cond" function from the environment must be treated separately. This
     * may be null if the function is not defined.
     */
    private @Nullable final Function condFunction;

    /**
     * the "$pattern" function from the environment must be treated separately. This
     * may be null if the function is not defined.
     */
    private final @Nullable Function patternFunction;

    /**
     * the equality function from the environment must be treated separately.
     * This may be null if the function is not defined
     */
    private final @Nullable Function equalityFunction;

    /**
     * the equality function from the environment must be treated separately.
     * This may be null if the function is not defined
     */
    private final @Nullable Function weakEqualityFunction;

    /**
     * All axioms as they are extracted from the environment.
     */
    private final Collection<Axiom> allAxioms;

    /**
     * All user specified inlined (that is given in SMT2) axioms are collected
     * here.
     */
    private final String inlinedAxioms;

    /**
     * All sorts as they are extraced from the environment.
     */
    private final List<Sort> allSorts;

    /**
     * This type visitor is used to translate a type into a term of type meta
     * type Type.
     *
     * For instance {@code set(int)} is translated to {@code (ty.set ty.int)}
     * and {@code product('a, list(bool))} becomes {@code (ty.product tyvar.a
     * (ty.list ty.bool)}.
     *
     * A type variable {@code 'a} is translated either to {@code tyvar.a} if the
     * variable is not under a quantifier and to {@code ?Type.a} if under a
     * quantifier.
     *
     * The quantification context is taken from the {@link #quantifiedVariables}
     * stack. If the parameter to the visitor is set to <code>true</code>, then
     * the translation treats every occurrence as bound.
     *
     */
    @SuppressWarnings("nullness")
    private final TypeVisitor<String, Boolean> typeToTerm = new TypeVisitor<String, Boolean>() {
        @Override
        public String visit(TypeApplication typeApplication, Boolean parameter)
                throws TermException {

            if (typeApplication.getArguments().size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("(ty.").append(typeApplication.getSort().getName());
                for (Type ty : typeApplication.getArguments()) {
                    sb.append(" ").append(ty.accept(this, parameter));
                }
                sb.append(")");
                return sb.toString();
            } else {
                return "ty." + typeApplication.toString();
            }
        }

        @Override
        public String visit(@NonNull TypeVariable typeVariable, @NonNull Boolean allVariables) {
            String variableName = typeVariable.getVariableName();
            assert quantifiedVariables != null : "QV";
            if (allVariables || quantifiedVariables.contains("?Type." + variableName)) {
                return "?Type." + variableName;
            } else {
                extrafuncs.add("tyvar." + variableName + " () Type");
                return "tyvar." + variableName;
            }
        }

        @Override
        public String visit(SchemaType schemaType, Boolean parameter)
                throws TermException {
            throw new TermException(schemaType + " is a schema type? The translated terms must be toplevel!");
        }
    };

    /**
     * Instantiates a new SMT-lib translator.
     *
     * @param env
     *            the environment to use.
     */
    public SMTLib2Translator(Environment env) {
        for (int i = 0; i < BUILTIN_FUNCTIONS.length; i += 2) {
            translationMap.put(BUILTIN_FUNCTIONS[i], BUILTIN_FUNCTIONS[i + 1]);
        }

        condFunction = env.getFunction("cond");
        patternFunction = env.getFunction("$pattern");
        equalityFunction = env.getFunction("$eq");
        weakEqualityFunction = env.getFunction("$weq");
        allAxioms = env.getAllAxioms();
        allSorts = env.getAllSorts();
        inlinedAxioms = extractInlinedAxioms(env);
    }

    /**
     * Identify inlined smt2 axioms from an environment.
     *
     * These are properties beginning with "smt2.inline."
     *
     * @param env
     *            the environment to scan
     * @return "" if nothing found. The content otherwise
     */
    private @NonNull String extractInlinedAxioms(Environment env) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : env.getAllProperties().entrySet()) {
            String key = entry.getKey();
            if(key.startsWith(INLINE_DIRECTIVE)) {
                sb.append("; -- Inlined axiom -- " + key + "\n");
                sb.append(entry.getValue());
                sb.append("\n; -- End -- " + key + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * Translate a term to its smt-lib counterpart.
     *
     * <p>
     * Possibly, sort and constant definitions are written to the appropriate
     * storages.
     *
     * @param term
     *            the term to translate
     *
     * @param asType
     *            defines which kind of expression the result has be assured to
     *            be in
     *
     * @return the string representing the translation
     *
     * @throws TermException
     *             if the translation fails for whatever reason
     */
    public String translate(Term term, ExpressionType asType)
            throws TermException {
        requestedType = asType;
        term.visit(this);
        if(resultingType != asType) {
            result = convert(result, resultingType, asType);
            resultingType = asType;
        }

        assert resultingType == asType;
        return result;
    }

    /**
     * Translate a sequent to its smt-lib counterpart, i.e., a formula.
     *
     * <p>
     * Possibly, sort and constant definitions are written to the appropriate
     * storages.
     *
     * <p>
     * The resulting formula is a conjunction of all formulas on the sequent's
     * lhs (positive) and all formulas of the rhs (negative). For the empty
     * sequent <code>"(and true)"</code> is returned.
     *
     * @param sequent
     *            the sequent to translate
     *
     * @return the string representing the translation
     *
     * @throws TermException
     *             if the translation fails for whatever reason
     */
    public String translate(@NonNull Sequent sequent) throws TermException {
        StringBuilder sb = new StringBuilder();
        sb.append("(and true ");
        for (Term term : sequent.getAntecedent()) {
            sb.append(translate(term, BOOL));
        }
        for (Term term : sequent.getSuccedent()) {
            sb.append("(not ");
            sb.append(translate(term, BOOL));
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
    @Override
    public void export(@NonNull Sequent sequent, @NonNull Appendable builder)
            throws TermException, IOException {

        extrafuncs.clear();
        assumptions.clear();
        quantifiedVariables.clear();

        includeTypes();
        includeAxioms();

        // do the translation already now s.t. all necessary entities get into
        // sorts &co.
        String translation = translate(sequent);

        builder.append("; created by ivil " + new Date());
        builder.append("; ");
        includePreamble(builder);

        for (String f : extrafuncs) {
            builder.append("(declare-fun " + f + ")\n");
        }

        builder.append(inlinedAxioms);
        exportAssumptions(builder);

        builder.append("\n; --- The sequent\n");
        builder.append("(assert " + indent(translation) + ")\n");
    }

    /*
     * Iterate through the list of axioms and export them to a stream.
     * Entries are optionally commented.
     */
    private void exportAssumptions(Appendable builder) throws IOException {
        for (String string : assumptions) {
            String[] lines = string.split("\n");
            for (int i = 0; i < lines.length - 1; i++) {
                builder.append("; --- " + lines[i] + "\n");
            }
            builder.append("(assert " + indent(lines[lines.length - 1])
                    + ")\n\n");
        }
    }

    /*
     * Add type meta symbols for all types of the environment.
     *
     * For every nullary type constructor C we get a constant "(ty.C Type)", for
     * every constructor D of higher arity a symbol with an according number of
     * parameters is added.
     *
     * Assumptions are added which ensure that the symbols are distinct and that
     * the constructors are injective functions.
     */
    // package visible for testing
    /* package */void includeTypes() throws IOException {
        int maxArity = 0;

        //
        // declare sort symbols
        for (Sort sort : allSorts) {
            int arity = sort.getArity();
            maxArity = Math.max(maxArity, arity);
            String name = sort.getName();
            if (!name.equals("int") && !name.equals("bool")) {
                // bool and are treated in preamble already
                extrafuncs.add("ty." + name + " ("
                        + Util.join(Collections.nCopies(arity, "Type"), " ")
                        + ") Type");
            }
        }

        //
        // sorts are injective functions
        StringBuilder sb = new StringBuilder();
        for (Sort sort : allSorts) {
            int arity = sort.getArity();
            if (arity > 0) {
                sb.setLength(0);
                sb.append("(forall (");
                for (int i = 0; i < arity; i++) {
                    sb.append("(?t" + i + " Type) ");
                }
                for (int i = 0; i < arity; i++) {
                    sb.append("(?u" + i + " Type) ");
                }
                sb.append(") (implies (= (ty.").append(sort.getName());
                for (int i = 0; i < arity; i++) {
                    sb.append(" ?t" + i);
                }
                sb.append(") (ty.").append(sort.getName());
                for (int i = 0; i < arity; i++) {
                    sb.append(" ?u" + i);
                }
                sb.append(")) (and");
                for (int i = 0; i < arity; i++) {
                    sb.append(" (= ?t" + i + " ?u" + i + ")");
                }
                sb.append(")))");
                assumptions.add("Sort symbol for " + sort + " is injective\n"
                        + sb.toString());
            }
        }

        //
        // type distinct
        sb.setLength(0);
        if (maxArity > 0) {
            sb.append("(forall (");
            for (int i = 0; i < maxArity; i++) {
                sb.append("(?t" + i + " Type) ");
            }
            sb.append(") (distinct");
            for (Sort sort : allSorts) {
                int arity = sort.getArity();
                if (arity > 0) {
                    sb.append(" (ty." + sort.getName());
                    for (int i = 0; i < arity; i++) {
                        sb.append(" ?t" + i);
                    }
                    sb.append(")");
                } else {
                    sb.append(" ty." + sort.getName());
                }
            }
            sb.append("))");
        } else {
            // easy case: no quantification
            sb.append("(distinct");
            for (Sort sort : allSorts) {
                sb.append(" ty.").append(sort.getName());
            }
            sb.append(")");
        }
        assumptions.add("Type symbols are distinct\n" + sb);
    }

    /*
     * load the resource "preamble.smt" and stream it to the stream.
     */
    private void includePreamble(Appendable pw) throws IOException {
        InputStream stream = getClass().getResourceAsStream("smt2_preamble.smt");
        if (stream == null) {
            throw new IOException("Resource smt2_preamble.smt not found");
        }
        Reader r = new InputStreamReader(stream);
        char[] buffer = new char[1024];
        int read = r.read(buffer);
        while (read != -1) {
            pw.append(new String(buffer, 0, read));
            read = r.read(buffer);
        }
    }

    /*
     * Add axioms from the environment to the set of assumptions.
     */
    private void includeAxioms() throws IOException, TermException {
        for (Axiom ax : allAxioms) {
            String translation = translate(ax.getTerm(), BOOL);
            assumptions.add("Axiom " + ax.getName() + " from environment\n"
                    + translation);
        }
    }

    /*
     * by default replace by a new unknown symbol.
     *
     * TODO cache replacement results => use same symbol for identical terms but
     * ... do this on a per-translation basis!
     *
     * TODO add typing axioms also for those elements?!
     *
     * We have to add bound variables as parameters! Otherwise the follwing will
     * be proven by SMT: <pre> (\forall x as int; ({a:=0}x)=x) -> c1=c2 </pre>
     */
    @Override
    protected void defaultVisitTerm(Term term) throws TermException {
        String name = "unknown" + unknownCounter;
        List<String> signature = new ArrayList<String>();
        unknownCounter++;

        if (quantifiedVariables.isEmpty()) {
            result = name;
        } else {
            for (String s : quantifiedVariables) {
                // find point.
                int point = s.indexOf('.');
                signature.add(s.substring(1, point));
            }
            result = "(" + name + " " + Util.join(quantifiedVariables, " ")
                    + ")";
        }

        extrafuncs.add(name + " (" + Util.join(signature, " ") + ") " + requestedType);
        resultingType = requestedType;
    }

    /*
     * Translate an application.
     * This is quite complicated.
     *
     * Special treatment for conditional terms and propositional junctors.
     */
    @Override
    public void visit(Application application) throws TermException {
        Function function = application.getFunction();
        String name = function.getName();
        ExpressionType myRequestedType = requestedType;

        if (function == condFunction) {
            StringBuilder sb = new StringBuilder();
            sb.append("(ite ");

            sb.append(translate(application.getSubterm(0), BOOL))
            .append(" ");

            sb.append(translate(application.getSubterm(1), myRequestedType))
            .append(" ");
            sb.append(translate(application.getSubterm(2), myRequestedType))
            .append(")");
            result = sb.toString();
            return;
        }

        if (function == patternFunction) {
            // ignore inner patterns (not in a quantifier)
            Log.log(Log.WARNING, "Inner pattern found, ignored: " + application);
            result = translate(application.getSubterm(1), myRequestedType);
            return;
        }

        if(function == equalityFunction || function == weakEqualityFunction) {
            // smt equality does not have a type argument but ivil has. Make it manually
            StringBuilder sb = new StringBuilder();
            sb.append("(= ");

            Term term1 = application.getSubterm(0);
            ExpressionType type1 = typeToExpressionType(term1.getType());
            Term term2 = application.getSubterm(1);
            ExpressionType type2 = typeToExpressionType(term1.getType());

            // If both the same type (that should always be the case
            // unless for weak equality), use that, otherwise
            // resort to UNIVERSE
            ExpressionType type = (type1==type2) ? type1 : UNIVERSE;

            sb.append(translate(term1, type))
            .append(" ")
            .append(translate(term2, type))
            .append(")");
            result = sb.toString();
            resultingType = BOOL;
            return;
        }

        if (function instanceof NumberLiteral) {
            result = function.getName();
            resultingType = INT;
            return;
        }

        String translation = translationMap.get(name);

        if (PROPOSITIONAL_PREDICATES.contains(translation)) {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(translation);
            for (Term subterm : application.getSubterms()) {
                sb.append(" ").append(translate(subterm, BOOL));
            }
            sb.append(")");

            result = sb.toString();
            resultingType = BOOL;
            return;
        }

        if (translation == null) {
            translation = makeExtraFunc(function);
            translationMap.put(name, translation);
        }

        boolean hasArgs = application.countSubterms() > 0;
        SortedSet<TypeVariable> typeVars = collectTypeVars(function);
        Type resultType = function.getResultType();

        if (!typeVars.isEmpty() || hasArgs) {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(translation);
            Type[] argTypes = function.getArgumentTypes();

            TermMatcher matcher = new TermMatcher();
            TypeMatchVisitor visitor = new TypeMatchVisitor(matcher);

            TypeUnification.makeSchemaVariant(resultType)
            .accept(visitor, application.getType());
            for (int i = 0; i < argTypes.length; i++) {
                TypeUnification.makeSchemaVariant(argTypes[i])
                .accept(visitor, application.getSubterm(i).getType());
            }

            for (TypeVariable tv : typeVars) {
                Type t = TypeUnification.makeSchemaVariant(tv);
                assert t instanceof SchemaType : "either tv was not a type variable or the specification of makeSchemaVariant changed";

                Type type = matcher.getTypeFor(((SchemaType) t).getVariableName());
                assert type != null : "non-nullness: t has been set by the type match visitor";

                String typeString = type.accept(typeToTerm, false);
                sb.append(" ").append(typeString);
            }

            List<Term> subterms = application.getSubterms();
            assert subterms.size() == argTypes.length;

            for (int i = 0; i < argTypes.length; i++) {
                sb.append(" ");
                boolean isInt = Environment.getIntType().equals(argTypes[i]);
                sb.append(translate(subterms.get(i), isInt ? INT : UNIVERSE));
            }

            sb.append(")");
            result = sb.toString();
        } else {
            result = translation;
        }

        if (ALL_PREDICATES.contains(translation) ||
                Environment.getBoolType().equals(resultType)) {
            resultingType = BOOL;
        } else if (Environment.getIntType().equals(resultType)) {
            resultingType = INT;
        } else {
            resultingType = UNIVERSE;
        }
    }

    @Override
    public void visit(Binding binding) throws TermException {
        Binder binder = binding.getBinder();
        String name = binder.getName();
        String translation = translationMap.get(name);

        if (QUANTIFIERS.contains(name)) {
            QuantificationTranslator trans = new QuantificationTranslator(translation);
            binding.visit(trans);
            trans.ensurePattern();
            result = trans.toString();
            resultingType = BOOL;
            return;
        }

        if(translation == null) {
            translation = makeExtraBinderFunc(binder);
            translationMap.put(name, translation);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(" + translation );

        // type arguments
        Type[] argTypes = binder.getArgumentTypes();
        TermMatcher matcher = new TermMatcher();
        TypeMatchVisitor visitor = new TypeMatchVisitor(matcher);

        TypeUnification.makeSchemaVariant(binder.getResultType())
        .accept(visitor, binding.getType());

        TypeUnification.makeSchemaVariant(binder.getVarType())
        .accept(visitor, binding.getVariableType());

        for (int i = 0; i < argTypes.length; i++) {
            TypeUnification.makeSchemaVariant(argTypes[i])
            .accept(visitor, binding.getSubterm(i).getType());
        }

        SortedSet<TypeVariable> typeVars = collectTypeVars(binder);
        for (TypeVariable tv : typeVars) {
            Type t = TypeUnification.makeSchemaVariant(tv);
            assert t instanceof SchemaType : "either tv was not a type variable or the specification of makeSchemaVariant changed";

            Type type = matcher.getTypeFor(((SchemaType) t).getVariableName());
            assert type != null : "non-nullness: t has been set by the type match visitor";

            String typeString = type.accept(typeToTerm, false);
            sb.append(" ").append(typeString);
        }
        sb.append(" ");

        // lambda arguments
        Type[] bndArgTypes = binder.getArgumentTypes();
        for (int i = 0; i < binder.getArity(); i++) {
            String boundTerm = makeBoundTerm(binding.getVariable(),
                    binding.getSubterm(i),
                    typeToExpressionType(binder.getVarType()),
                    typeToExpressionType(bndArgTypes[i]));
            sb.append(boundTerm + " ");
        }
        sb.append(")");

        result = sb.toString();
        resultingType = typeToExpressionType(binder.getResultType());
    }

    private final Map<Pair<BindableIdentifier, Term>, String> boundTermMap =
            new HashMap<Pair<BindableIdentifier,Term>, String>();

    private String makeBoundTerm(BindableIdentifier var, Term term,
            ExpressionType varType, ExpressionType targetType) throws TermException {
        String content = translate(term, targetType);

        Pair<BindableIdentifier, Term> key = Pair.make(var, term);
        String cache = boundTermMap.get(key);
        if(cache != null) {
            return cache;
        }

        // if not cached: generate a fresh skolem lambda constant and specify it.
        String name = "lambda." + boundTermMap.size();

        SortedSet<Variable> vars = new TreeSet<Variable>(STRING_COMPARATOR);
        vars.addAll(FreeVarFinder.getFreeVariables(term));
        vars.remove(var);
        StringBuilder sbTypeArgs = new StringBuilder();
        StringBuilder sbVarDecl = new StringBuilder();
        StringBuilder sbCallArgs = new StringBuilder();
        for (Variable v : vars) {
            Type t = v.getType();
            ExpressionType exT = typeToExpressionType(t);

            v.visit(this);
            String realName = result;

            sbTypeArgs.append(" ").append(exT);
            sbVarDecl.append(" (").append(realName).append(" ")
                    .append(exT).append(")");
            sbCallArgs.append(" ").append(realName);
        }

        extrafuncs.add(name + " (" + sbTypeArgs + ") (Array " +
                varType +
                " " + targetType +")");

        // parenthesis only allowed if args
        String lambdaCall = (sbCallArgs.length() > 0) ?
                "(" + name + sbCallArgs + ")" :
                name;

        var.visit(this);
        assumptions.add("Lambda-Definition of " + name + "\n" +
                "(forall ((?x " + varType + ")" +
                sbVarDecl + ") " +
                "(let ((" + result + " " + convert("?x", varType, resultingType) + ")) " +
                "(= (select " + lambdaCall + " ?x) " +
                content + ")))");


        // parentheses are only allowed with arguments
        String retVal;
        if(sbCallArgs.length() > 0) {
            retVal = "(" + name + sbCallArgs + ")";
        } else {
            retVal = name;
        }
        boundTermMap.put(key, retVal);
        return retVal;
    }

    @Override
    public void visit(Variable variable) throws TermException {
        resultingType = typeToExpressionType(variable.getType());
        String name = variable.getName();
        result = "?" + resultingType + "." + name;
    }

    @Override
    public void visit(TypeVariableBinding tBinding)
            throws TermException {
        String translation = translationMap.get(tBinding.getKind().toString());

        assert translation != null : "All type quantifiers must be defined";

        QuantificationTranslator trans = new QuantificationTranslator(translation);
        tBinding.visit(trans);
        trans.ensurePattern();
        result = trans.toString();
        resultingType = BOOL;
    }

    private String makeExtraBinderFunc(Binder binder) throws TermException {
        String binderName = binder.getName();
        // drop the initial "\\"
        String name = "bnd." + binderName.substring(1);
        name = name.replace('$', '.');

        // var type
        Type fctVarType = binder.getVarType();
        ExpressionType varType = typeToExpressionType(fctVarType);

        // result type
        Type fctResultType = binder.getResultType();
        ExpressionType resultType = typeToExpressionType(fctResultType);

        // argument types
        Type[] fctArgTypes = binder.getArgumentTypes();
        ExpressionType[] argTypes = new ExpressionType[fctArgTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = typeToExpressionType(fctArgTypes[i]);
        }

        SortedSet<TypeVariable> allTypeVariables = collectTypeVars(binder);

        List<String> types = new ArrayList<String>();
        types.addAll(Collections.nCopies(allTypeVariables.size(), "Type"));
        for (ExpressionType exTy : argTypes) {
            types.add("(Array " + varType + " " + exTy.toString() + ")");
        }

        extrafuncs.add(name + " (" + Util.join(types, " ") + ") " + resultType);

        // typing only for Universe binders
        if (resultType == UNIVERSE) {
            StringBuilder sb = new StringBuilder();

            sb.append("Typing for binder symbol ").append(name).append("\n");

            assert argTypes.length > 0 : "Binder w/o arguments are not considered here";

            sb.append("(forall (");

            for (TypeVariable typeVariable : allTypeVariables) {
                sb.append("(?Type.").append(typeVariable.getVariableName()).append(" Type) ");
            }

            for (int i = 0; i < argTypes.length; i++) {
                sb.append("(?x").append(i).append(" ")
                // types start with the type variables types and then the array types
                .append(types.get(i + allTypeVariables.size()))
                .append(") ");
            }

            sb.append(") (! (ty ");

            StringBuilder fctcallsb = new StringBuilder();
            {
                fctcallsb.append("(").append(name);
                for (TypeVariable typeVariable : allTypeVariables) {
                    fctcallsb.append(" ?Type.")
                    .append(typeVariable.getVariableName());
                }
                for (int i = 0; i < argTypes.length; i++) {
                    fctcallsb.append(" ?x").append(i);
                }
                fctcallsb.append(")");
            }
            sb.append(fctcallsb).append(" ")
            .append(fctResultType.accept(typeToTerm, true))
            .append(") :pattern (")
            .append(fctcallsb)
            .append(")))");

            assumptions.add(sb.toString());
        }
        return name;
    }

    private String makeExtraFunc(Function function) throws TermException {
        String fctName = function.getName();
        String name = "fct." + fctName;
        name = name.replace('$', '.');

        Type fctResultType = function.getResultType();
        ExpressionType resultType = typeToExpressionType(fctResultType);

        Type[] fctArgTypes = function.getArgumentTypes();
        ExpressionType[] argTypes = new ExpressionType[fctArgTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = typeToExpressionType(fctArgTypes[i]);
        }

        SortedSet<TypeVariable> allTypeVariables = collectTypeVars(function);

        List<String> types = new ArrayList<String>();
        types.addAll(Collections.nCopies(allTypeVariables.size(), "Type"));
        for (ExpressionType exTy : argTypes) {
            types.add(exTy.toString());
        }

        extrafuncs.add(name +" (" + Util.join(types, " ") + ") " + resultType);

        // only for Universe functions
        if (resultType == UNIVERSE) {
            StringBuilder sb = new StringBuilder();
            //   boolean varInResult = !resultTypeVariables.isEmpty() && argTypes.length > 0;

            sb.append("Typing for function symbol ").append(name).append("\n");

            if (allTypeVariables.isEmpty() && argTypes.length == 0) {

                // for a monomorphic constant symbols "bool c" add
                // "(= (ty fct.c) ty.bool)"
                sb.append("(ty ").append(name).append(" ").
                append(fctResultType.accept(typeToTerm, false)).
                append(")");

            } else {
                sb.append("(forall (");

                for (TypeVariable typeVariable : allTypeVariables) {
                    sb.append("(?Type.").append(typeVariable.getVariableName()).append(" Type) ");
                }

                for (int i = 0; i < argTypes.length; i++) {
                    sb.append("(?x").append(i).append(" ").append(argTypes[i])
                    .append(") ");
                }

                sb.append(") (! (ty ");

                StringBuilder fctcallsb = new StringBuilder();
                {
                    fctcallsb.append("(").append(name);
                    for (TypeVariable typeVariable : allTypeVariables) {
                        fctcallsb.append(" ?Type.")
                            .append(typeVariable.getVariableName());
                    }
                    for (int i = 0; i < argTypes.length; i++) {
                        fctcallsb.append(" ?x").append(i);
                    }
                    fctcallsb.append(")");
                }
                sb.append(fctcallsb).append(" ")
                        .append(fctResultType.accept(typeToTerm, true))
                        .append(") :pattern (")
                        .append(fctcallsb)
                        .append(")))");
            }
            assumptions.add(sb.toString());
        }

        // Add for uniqueness
        // TODO unique functions are injective!
        // for a function f(a,b,c) introduce 3 inverse functions invfct1.f(f(a,b,c)) = a
        if(function.isUnique()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Uniqueness of function symbol ").append(name).append("\n");

            if (argTypes.length == 0) {
                sb.append("(= (unique ").append(name).append(") ").append(uniqueCounter).append(")");

            } else {
                sb.append("(forall (");

                for (int i = 0; i < argTypes.length; i++) {
                    sb.append("(?x").append(i).append(" ").append(argTypes[i])
                    .append(") ");
                }

                sb.append(") (= (unique (").append(name);
                for (int i = 0; i < argTypes.length; i++) {
                    sb.append(" ?x").append(i);
                }
                sb.append(")) ").append(uniqueCounter).append("))");
            }

            uniqueCounter++;
            assumptions.add(sb.toString());
        }


        return name;
    }

    private SortedSet<TypeVariable> collectTypeVars(Function function) {
        Set<TypeVariable> resulttypeVariables =
                TypeVariableCollector.collect(function.getResultType());

        Set<TypeVariable> argumentTypeVariables =
                TypeVariableCollector.collect(
                        Util.readOnlyArrayList(function.getArgumentTypes()));

        SortedSet<TypeVariable> allTypeVariables = new TreeSet<TypeVariable>(STRING_COMPARATOR);
        allTypeVariables.addAll(resulttypeVariables);
        allTypeVariables.addAll(argumentTypeVariables);
        return allTypeVariables;
    }

    private SortedSet<TypeVariable> collectTypeVars(Binder binder) {
        Set<TypeVariable> resulttypeVariables =
                TypeVariableCollector.collect(binder.getResultType());

        Set<TypeVariable> vartypeVariables =
                TypeVariableCollector.collect(binder.getVarType());

        Set<TypeVariable> argumentTypeVariables =
                TypeVariableCollector.collect(
                        Util.readOnlyArrayList(binder.getArgumentTypes()));

        SortedSet<TypeVariable> allTypeVariables = new TreeSet<TypeVariable>(STRING_COMPARATOR);
        allTypeVariables.addAll(resulttypeVariables);
        allTypeVariables.addAll(vartypeVariables);
        allTypeVariables.addAll(argumentTypeVariables);

        return allTypeVariables;
    }


    //    /*
    //     * calculate the difference between two sets.
    //     * Only creates a new Object if the difference is not empty.
    //     */
    //    private <E> Set<E> setDifference(Set<E> set, Set<E> toSubtract) {
    //
    //        if(toSubtract.containsAll(set)) {
    //            return Collections.emptySet();
    //        } else {
    //            Set<E> result = new HashSet<E>(set);
    //            result.removeAll(toSubtract);
    //            return result;
    //        }
    //    }

//    /**
//     * Map a logic type to a smt type.
//     *
//     * @param type
//     *            the logical type
//     *
//     * @return the string
//     */
//    private @NonNull String makeSort(@NonNull Type type) {
//        if (Environment.getIntType().equals(type)) {
//            return "Int";
//        } else if(Environment.getBoolType().equals(type)) {
//            return "Bool";
//        } else {
//            return "Universe";
//        }
//    }

    /**
     * Deduce the expression type from a smt type string.
     *
     * This maps {@code "Int"} to {@link ExpressionType#INT} and {@code
     * "Universe"} to {@link ExpressionType#UNIVERSE}.
     *
     * @param sort
     *            the sort to translate
     *
     * @return the according expression type
     *
     * @throws IllegalArgumentException
     *             if the argument is neither {@code "Int"} nor {@code
     *             "Universe"}.
     */
//    private @NonNull ExpressionType sortToExpressionType(@NonNull String sort) {
//        if ("Int".equals(sort)) {
//            return INT;
//        } else if ("Universe".equals(sort)) {
//            return UNIVERSE;
//        } else if ("Bool".equals(sort)) {
//            return BOOL;
//        } else {
//            throw new IllegalArgumentException(sort);
//        }
//    }

    private ExpressionType typeToExpressionType(Type type) {
        if (Environment.getIntType().equals(type)) {
            return INT;
        } else if(Environment.getBoolType().equals(type)) {
            return BOOL;
        } else {
            return UNIVERSE;
        }
    }

    /**
     * Convert an expression of some expression type into a possibly different
     * type.
     *
     * @param expr
     *            the expression as string
     * @param from
     *            the expression type in which the first argument is to be read
     * @param to
     *            the expression type to convert the argument to
     *
     * @return the converted expression
     *
     * @throws RuntimeException if an undoable conversion is requested (likely a bug)
     */
    @SuppressWarnings("fallthrough")
    private @NonNull String convert(@NonNull String expr,
            @NonNull ExpressionType from, @NonNull ExpressionType to) {

        switch (from) {
        case UNIVERSE:
            switch (to) {
            case UNIVERSE:
                return expr;
            case BOOL:
                return "(u2b " + expr + ")";
            case INT:
                return "(u2i " + expr + ")";
            }
        case BOOL:
            switch (to) {
            case UNIVERSE:
                return "(b2u " + expr + ")";
            case BOOL:
                return expr;
            case INT:
                throw new RuntimeException("This cannot be converted: " + expr
                        + " from BOOL to INT");
            }
        case INT:
            switch (to) {
            case UNIVERSE:
                return "(i2u " + expr + ")";
            case BOOL:
                throw new RuntimeException("This cannot be converted: " + expr
                        + " from INT to BOOL");
            case INT:
                return expr;
            }
        }

        Log.log(Log.ERROR, "Cannot convert: " + expr + ", " + from + " -> "
                + to);
        throw new Error("FALLTHROUGH, can never happen");
    }

    /*
     * pretty print a smt lisp-style expression on several lines
     */
    @SuppressWarnings("fallthrough")
    public static String indent(String string) {

        StringBuilder sb = new StringBuilder();
        int indention = 0;
        char last = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
            case '(':
                if(last != '(') {
                    // new line only if not another '('
                    sb.append("\n");
                    for (int j = 0; j < indention; j++) {
                        sb.append(" ");
                    }
                }
                sb.append("(");
                indention++;
                break;
            case ':':
                // :pattern and similar
                sb.append("\n");
                for (int j = 0; j < indention; j++) {
                    sb.append(" ");
                }
                sb.append(":");
                break;
            case ')':
                indention--;
                // fall through
            default:
                sb.append(c);
            }
            last = c;
        }

        return sb.toString();
    }

    private class QuantificationTranslator extends DefaultTermVisitor {

        private final String binderContext;
        private final List<String> innerVars;
        private final List<String> guards;
        private String pattern;
        private String result;

        public QuantificationTranslator(String binderContext) {
            this.binderContext = binderContext;
            this.guards = new ArrayList<String>();
            this.innerVars = new ArrayList<String>();
        }

        /**
         * If this visitor has not yet brought up a pattern (e.g. by $pattern),
         * use the typing guards as pattern. This is better than nothing.
         *
         * If the guards are empty, the pattern remains <code>null</code>.
         */
        public void ensurePattern() {
            if(pattern == null && !guards.isEmpty()) {
                pattern = Util.join(guards, " ");
            }
        }

        @Override
        protected void defaultVisitTerm(Term term) throws TermException {
            // let the outer visitor do the job then
            this.result = translate(term, BOOL);
        }

        @Override
        public void visit(Binding binding) throws TermException {
            Binder binder = binding.getBinder();
            String name = binder.getName();
            @Nullable String translation = translationMap.get(name);

            // only exists and forall are defined and we are limited to one
            // context
            if (!binderContext.equals(translation)) {
                defaultVisitTerm(binding);
                return;
            }

            Term innerFormula = binding.getSubterm(0);
            BindableIdentifier bindable = binding.getVariable();
            assert bindable instanceof Variable;

            Type varType = bindable.getType();
            ExpressionType boundType = typeToExpressionType(varType);
            String var = "?" + boundType + "." + bindable.getName();
            innerVars.add("(" + var + " " + boundType + ")");

            addTypeGuard(var, varType);

            quantifiedVariables.push(var);
            innerFormula.visit(this);
            quantifiedVariables.pop();
        }

        @Override
        public void visit(TypeVariableBinding tBinding)
                throws TermException {
            String translation = translationMap.get(tBinding.getKind().toString());

            // only exists and forall are defined and we are limited to one
            // context, otherwise resort to the default treatment
            if (!binderContext.equals(translation)) {
                defaultVisitTerm(tBinding);
                return;
            }

            Term innerFormula = tBinding.getSubterm();
            Type boundType = tBinding.getBoundType();
            // check that not a schema type.
            assert boundType instanceof TypeVariable :
                "Only bound type vars are supported!";

            String var = "?Type." + ((TypeVariable)boundType).getVariableName();
            innerVars.add("(" + var + " Type)");

            quantifiedVariables.push(var);
            innerFormula.visit(this);
            quantifiedVariables.pop();
        }

        @Override
        public void visit(Application application) throws TermException {
            Function function = application.getFunction();
            if(function == patternFunction) {
            StringBuilder sb = new StringBuilder();
            sb.append("(! ");
                Term pattern = application.getSubterm(0);
                Type patternType = pattern.getType();
                ExpressionType patternExpType = typeToExpressionType(patternType);
                // give a pattern the type it already has: no conversion!
                String patternTrans = translate(pattern, patternExpType);

                // the value needs to be boolean
                Term value = application.getSubterm(1);
                String valueTrans = translate(value, BOOL);

                this.result = valueTrans;
                this.pattern = patternTrans;
            } else {
                defaultVisitTerm(application);
            }
        }

        /**
         * Add to the list of type guards the check that "(ty var varType)".
         */
        private void addTypeGuard(String var, Type varType) throws TermException {
            ExpressionType expType = typeToExpressionType(varType);
            if (expType == UNIVERSE) {

                String guard = "(ty " + var + " "
                        + varType.accept(typeToTerm, false) +
                        ")";
                guards.add(guard);
            }
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();

            // The quantifier
            sb.append("(").append(binderContext).append(" ");

            // The bound variables
            assert !innerVars.isEmpty();
            sb.append("(").append(Util.join(innerVars, " ")).append(") ");

            // The pattern head (if present)
            if(pattern != null) {
                sb.append("(! ");
            }

            // The guards (if present)
            if(!guards.isEmpty()) {
                String conj = "forall".equals(binderContext) ? "(implies"
                        : "(and";
                sb.append(conj).append(" (and ");
                sb.append(Util.join(guards, " "));
                sb.append(") ");
            }

            // the embedded formula
            sb.append(result);

            // the tail of guards (if present)
            if(!guards.isEmpty()) {
                sb.append(")");
            }

            // the tail of pattern (if present)
            if(pattern != null) {
                sb.append(" :pattern (").append(pattern).append("))");
            }

            sb.append(")");
            return sb.toString();
        }
    }
}

/**
 * This visitor is used to traverse the term. It calculates the set of free
 * variables.
 *
 * FIXME remove the redundancy
 *
 * @see NoFreeVars.FreeVarChecker
 */
class FreeVarFinder extends DefaultTermVisitor.DepthTermVisitor {

    /**
     * The bound variables.
     */
    private final Stack<Variable> boundVariables = new Stack<Variable>();

    /**
     * The free variables.
     */
    private final Set<Variable> freeVariables = new HashSet<Variable>();

    @Override
    public void visit(Binding binding) throws TermException {
        if (binding.getVariable() instanceof Variable) {
            Variable variable = (Variable) binding.getVariable();
            boundVariables.push(variable);
            super.visit(binding);
            boundVariables.pop();
        } else {
            // if schema variable bound
            // LOG if we use logging once
            Log.log(Log.WARNING,
                    "We should actually only check unschematic terms, but: "
                            + binding);
            super.visit(binding);
        }
    }

    @Override
    public void visit(Variable variable) throws TermException {
        if (!boundVariables.contains(variable)) {
            freeVariables.add(variable);
        }
    }

    public static Set<Variable> getFreeVariables(Term term) {
        FreeVarFinder f = new FreeVarFinder();
        try {
            term.visit(f);
        } catch (TermException e) {
            // bever thrown
            throw new Error();
        }
        return f.freeVariables;
    }
}
