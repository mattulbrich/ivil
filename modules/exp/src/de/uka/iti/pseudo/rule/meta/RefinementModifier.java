/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.ProgramChanger;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.meta.RefinementMarkInfoCollector.MarkInfo;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.DefaultStatementVisitor;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.HavocStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.term.statement.StatementVisitor;
import de.uka.iti.pseudo.util.TermUtil;
import de.uka.iti.pseudo.util.settings.Settings;

final class RefinementModifier {

    private static final Settings S = Settings.getInstance();
    public static final String ABSTRACT_MARK_NAME =
            S.getProperty("pseudo.refinement.markAbstract", "$markA");
    public static final String CONCRETE_MARK_NAME =
            S.getProperty("pseudo.refinement.markConcrete", "$markC");

    private static final Comparator<MarkInfo> INDEX_REVERSE_ORDER =
            new Comparator<MarkInfo>() {
        @Override
        public int compare(MarkInfo o1, MarkInfo o2) {
            return o2.index - o1.index;
        }
    };

    private final @NonNull LiteralProgramTerm programTerm;
    private final @NonNull LiteralProgramTerm innerProgramTerm;
    private final @NonNull Term postcondition;

    private @NonNull Function markConcreteProgvar;
    private @NonNull Function markAbstractProgvar;
    // this variable is created lazily as its type is not known a priori
    private @Nullable Function variantProgvar;
    private final @Nullable Term initialVariant;
    private final String skipMarkIndidicator;
    private Map<Integer, MarkInfo> markInfoConcrete;
    private Map<Integer, MarkInfo> markInfoAbstract;
    private final Environment env;
    private final TermFactory tf;
    private final Set<Function> modifiedProgramVars = new HashSet<Function>();

    /**
     * This visitor collects program variables written within a program. They
     * are "written" to by assignments and havocs. This is needed to construct
     * the anonymising update.
     */
    private final StatementVisitor writtenProgramVariableFinder = new DefaultStatementVisitor() {

        @Override
        public void visit(HavocStatement havocStatement) throws TermException {
            Function progvar = TermUtil.getFunction(havocStatement.getTarget());
            modifiedProgramVars.add(progvar);
        }

        @Override
        public void visit(AssignmentStatement assignmentStatement) throws TermException {
            for (Assignment ass: assignmentStatement.getAssignments()) {
                modifiedProgramVars.add(TermUtil.getFunction(ass.getTarget()));
            }
        }

        @Override
        protected void visitDefault(Statement statement) {
        }
    };

    public RefinementModifier(Environment env, Term term, Term initialVariant) throws TermException {

        this.env = env;
        this.tf = new TermFactory(env);

        if (term instanceof LiteralProgramTerm) {
            this.programTerm = (LiteralProgramTerm) term;
        } else {
            throw new TermException("Argument needs to be a program term");
        }

        if (programTerm.getSubterm(0) instanceof LiteralProgramTerm) {
            innerProgramTerm = (LiteralProgramTerm) programTerm.getSubterm(0);
        } else {
            throw new TermException("Post-condition needs to be a program term");
        }

        if (programTerm.getModality() != Modality.BOX) {
            throw new TermException("Outer modality needs to be [.]");
        }

        if (innerProgramTerm.getModality() != Modality.DIAMOND) {
            throw new TermException("Outer modality needs to be [<.>]");
        }

        this.initialVariant = initialVariant;
        this.markConcreteProgvar = addFreshProgVar(CONCRETE_MARK_NAME, Environment.getIntType());
        this.markAbstractProgvar = addFreshProgVar(ABSTRACT_MARK_NAME, Environment.getIntType());
        this.postcondition = innerProgramTerm.getSubterm(0);

        this.skipMarkIndidicator = env.getProperty("skipmark.refinement");
        if(this.skipMarkIndidicator == null) {
            throw new TermException("The property 'skipmark.refinement' has not been set");
        }
    }

    private Function addFreshProgVar(String pattern, Type type) throws TermException {
        try {
            String name = env.createNewFunctionName(pattern);
            Function result = new Function(name, type,
                    new Type[0], false, true, ASTLocatedElement.CREATED);
            env.addFunction(result);
            return result;
        } catch (EnvironmentException e) {
            throw new TermException("Cannot create function symbol " + pattern, e);
        }
    }

    public Term apply() throws TermException {
        assert markAbstractProgvar != null;
        assert markConcreteProgvar != null;

        collectMarkInfo();

        if(!markInfoAbstract.keySet().equals(markInfoConcrete.keySet())) {
            String msg = "The sets of used marker literals differ:\n" +
                    "Abstract (" + markAbstractProgvar + "): " + markInfoAbstract.keySet() +
                    "\nRefined (" + markConcreteProgvar + "): " + markInfoConcrete.keySet();
            throw new TermException(msg);
        }

        identifyModifiedProgVars();

        Term glueInv = prepareGlue();
        Update anonUpd = prepareAnonUpdate();
        Update markUpdate = prepareMarkUpdate();
        Program concrPrime = modifyProgram(programTerm.getProgram(),
                markInfoConcrete, markConcreteProgvar);
        Program abstrPrime = modifyProgram(innerProgramTerm.getProgram(),
                markInfoAbstract, markAbstractProgvar);

        Term result = makeProofObligations(concrPrime, abstrPrime, anonUpd, markUpdate, glueInv);

        return result;
    }

    public void setMarkFunctions(Function markAbstractProgvar, Function markConcreteProgvar) {
        this.markAbstractProgvar = markAbstractProgvar;
        this.markConcreteProgvar = markConcreteProgvar;
    }

    private Term makeProofObligations(Program concrPrime, Program abstrPrime,
            Update anonUpd, Update markUpdate, Term glue) throws TermException {

        int indexAbs = innerProgramTerm.getProgramIndex();
        Term a = LiteralProgramTerm.getInst(indexAbs,
                innerProgramTerm.getModality(), abstrPrime, glue);

        int indexConcr = programTerm.getProgramIndex();
        Term c = LiteralProgramTerm.getInst(indexConcr,
                programTerm.getModality(), concrPrime, a);

        if(initialVariant != null) {
            Update varUpd = makeVariantUpdate(initialVariant);
            c = tf.upd(varUpd, c);
        }

        Term result = tf.upd(markUpdate, c);

        for (Integer literal : markInfoAbstract.keySet()) {
            MarkInfo absInfo = markInfoAbstract.get(literal);
            a = LiteralProgramTerm.getInst(getTargetIndex(markInfoAbstract, absInfo.index),
                    innerProgramTerm.getModality(), abstrPrime, glue);

            indexConcr = markInfoConcrete.get(literal).index;
            c = LiteralProgramTerm.getInst(getTargetIndex(markInfoConcrete, indexConcr),
                    programTerm.getModality(), concrPrime, a);

            Term inv = absInfo.couplingInvariant;
            Term t = c;
            if(inv != null) {
                t = tf.impl(inv, t);
            }

            Term variant = absInfo.couplingVar;
            if(variant != null) {
                Update varUpd = makeVariantUpdate(variant);
                t = tf.upd(varUpd, t);
            }

            Term term = tf.upd(anonUpd, tf.upd(markUpdate, t));

            result = tf.and(result, term);
        }
        return result;
    }

    private int getTargetIndex(Map<Integer, MarkInfo> infos, int index) {
        int result = index + 2;
        for (MarkInfo info : infos.values()) {
            if(info.index < index) {
                result ++;
            }
        }
        return result;
    }

    private Update prepareMarkUpdate() throws TermException {
        Term zero = tf.number(0);
        return new Update(Arrays.asList(new Assignment(tf.cons(markAbstractProgvar), zero),
                new Assignment(tf.cons(markConcreteProgvar), zero)));
    }

    private Update makeVariantUpdate(Term variant) throws TermException {
        ensureVariantProgVar(variant);
        Term target = tf.cons(variantProgvar);
        return new Update(Collections.singletonList(new Assignment(target, variant)));
    }

    /*
     * makes sure the variant prog var exists or creates it.
     * If the type is not that of the argument, throw an exception.
     */
    private void ensureVariantProgVar(Term variant) throws TermException {
        if(variantProgvar == null) {
            variantProgvar = addFreshProgVar("var", variant.getType());
        } else {
            if(!variantProgvar.getResultType().equals(variant.getType())) {
                throw new UnificationException("Variants of two different types" +
                        " have been used in refinement.",
                        variantProgvar.getResultType(), variant.getType());
            }
        }
    }

    /**
     * Identify modified programvariables using the
     * {@link #writtenProgramVariableFinder}.
     *
     * The result is added to
     * This list explicitly excludes the marking variables.
     *
     * @throws TermException
     *             never thrown, only declared
     */
    private void identifyModifiedProgVars() throws TermException {
        programTerm.getProgram().visitStatements(writtenProgramVariableFinder);
        innerProgramTerm.getProgram().visitStatements(writtenProgramVariableFinder);
        modifiedProgramVars.remove(markAbstractProgvar);
        modifiedProgramVars.remove(markConcreteProgvar);
    }

    private void collectMarkInfo() throws TermException {
        try {
            this.markInfoConcrete =
                    RefinementMarkInfoCollector.collectMarkAssignments(programTerm.getProgram(),
                            skipMarkIndidicator);
        } catch (TermException e) {
            throw new TermException("Cannot collect mark info in " +
                    programTerm.getProgram().getName() + ": " +
                    e.getMessage(), e);
        }

        try {
            this.markInfoAbstract =
                    RefinementMarkInfoCollector.collectMarkAssignments(innerProgramTerm.getProgram(),
                            skipMarkIndidicator);
        } catch (TermException e) {
            throw new TermException("Cannot collect mark info in " +
                    innerProgramTerm.getProgram().getName() + ": " +
                    e.getMessage(), e);
        }
    }

    private Program modifyProgram(Program program,
            Map<Integer, MarkInfo> infos,
            Function markFunction) throws TermException {

        SortedSet<MarkInfo> infoList = new TreeSet<MarkInfo>(INDEX_REVERSE_ORDER);
        infoList.addAll(infos.values());

        ProgramChanger pc = new ProgramChanger(program, env);
        for (MarkInfo info : infoList) {
            int line = pc.getStatementAt(info.index).getSourceLineNumber();
            {
                // "mark := literal"
                pc.replaceAt(info.index, new AssignmentStatement(line,
                        tf.cons(markFunction), tf.number(info.literal)),
                        "Marker for refinement");
            }
            {
                // end
                pc.insertAfter(info.index, new EndStatement(line), "End for refinement");
            }
        }

        Program result;
        String name = env.createNewProgramName(program.getName());
        try {
            result = pc.makeProgram(name);
            env.addProgram(result);
        } catch (EnvironmentException e) {
            throw new TermException("Exception while rewriting program " + program.getName(), e);
        }

        return result;
    }

    private Update prepareAnonUpdate() throws TermException {
        List<Assignment> assignments = new ArrayList<Assignment>();
        for (Function progVar : modifiedProgramVars) {
            try {
                String newName = env.createNewFunctionName(progVar.getName());
                Function skolem = new Function(newName, progVar.getResultType(), new Type[0],
                        false, false, ASTLocatedElement.CREATED);
                env.addFunction(skolem);
                assignments.add(new Assignment(tf.cons(progVar), tf.cons(skolem)));
            } catch(EnvironmentException e) {
                throw new TermException("Exception while preparing anonymising update", e);
            }
        }

        return new Update(assignments);
    }

    private Term prepareGlue() throws TermException {
        Term result = tf.eq(tf.cons(markAbstractProgvar), tf.cons(markConcreteProgvar));

        // the abstract mark info contains the coupling invariants
        // and variants
        for (MarkInfo info : markInfoAbstract.values()) {
            Term inv = info.couplingInvariant;
            if(inv == null) {
                inv = Environment.getTrue();
            }
            Term var = info.couplingVar;
            Term varCheck;
            if(var == null) {
                varCheck = Environment.getTrue();
            } else {
                ensureVariantProgVar(var);
                varCheck = tf.prec(var, tf.cons(variantProgvar));
            }
            Term eq = tf.eq(tf.cons(markAbstractProgvar), tf.number(info.literal));
            Term impl = tf.impl(eq, tf.and(inv, varCheck));
            result = tf.and(result, impl);
        }

        // and finally the postcondition (as "0")
        {
            Term eq = tf.eq(tf.cons(markAbstractProgvar), tf.number(0));
            Term impl = tf.impl(eq, postcondition);
            result = tf.and(result, impl);
        }

        return result;
    }

    /**
     * Get the concrete mark information. For testing purposes.
     *
     * @return the markInfoConcrete
     */
    public Map<Integer, MarkInfo> getMarkInfoConcrete() {
        return Collections.unmodifiableMap(markInfoConcrete);
    }

    /**
     * @return the markInfoAbstract
     */
    public Map<Integer, MarkInfo> getMarkInfoAbstract() {
        return Collections.unmodifiableMap(markInfoAbstract);
    }

}