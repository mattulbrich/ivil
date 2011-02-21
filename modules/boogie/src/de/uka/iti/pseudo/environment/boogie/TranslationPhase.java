package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.boogie.ProgramMaker.StatementTripel;
import de.uka.iti.pseudo.environment.creation.ProgramChanger;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;

public final class TranslationPhase {

    /**
     * Name used in ivil; this is necessary, as ivil has only one global
     * namespace.
     */
    public final Map<VariableDeclaration, String> variableNames = new HashMap<VariableDeclaration, String>();

    /**
     * Terms, that correspond to expressions.
     */
    public final Map<Expression, Term> terms = new HashMap<Expression, Term>();

    /**
     * Statement triples for procedure declarations. Body might be empty.
     */
    public final Map<ProcedureDeclaration, ProgramMaker.StatementTripel> declarations = new HashMap<ProcedureDeclaration, ProgramMaker.StatementTripel>();

    /**
     * Statement triples for procedure implementations.
     */
    public final Map<ProcedureImplementation, ProgramMaker.StatementTripel> implementations = new HashMap<ProcedureImplementation, ProgramMaker.StatementTripel>();

    private List<Program> problematicPrograms = new LinkedList<Program>();

    private Term problem = null;

    /**
     * Creates a program out of statements and annotations and registers the
     * result in the environment as name.
     * 
     * @param statements
     * @param annotations
     * @param name
     * @throws EnvironmentCreationException
     */
    Program registerProgram(EnvironmentCreationState state, List<Statement> statements, List<String> annotations,
            String name, ASTElement node, int returnIndex) throws EnvironmentCreationException {
        Map<String, Integer> labels = new HashMap<String, Integer>();
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof SkipStatement) {
                String meta = annotations.get(i);
                if (null == meta)
                    continue;

                if (meta.startsWith("$label")) {
                    meta = meta.replace("$label:", "");
                    if (labels.containsKey(meta))
                        throw new EnvironmentCreationException("duplicate definition of label " + meta);

                    labels.put(meta, i);

                }
            }
        }
        // replace skip $goto
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof SkipStatement) {
                String meta = annotations.get(i);
                if (null == meta)
                    continue;

                if (meta.startsWith("$goto")) {
                    List<Integer> targets = new LinkedList<Integer>();
                    for (String s : meta.replace("$goto;", "").split(";")) {
                        if (!labels.containsKey(s))
                            throw new EnvironmentCreationException("cannot jump to undefined label " + s);

                        targets.add(labels.get(s));
                    }

                    Term[] args = new Term[targets.size()];

                    try {
                        for (int index = 0; index < args.length; index++) {
                            args[index] = new Application(state.env.getNumberLiteral(targets.get(index).toString()),
                                    Environment.getIntType());
                        }

                        statements.set(i, new de.uka.iti.pseudo.term.statement.GotoStatement(statements.get(i)
                                .getSourceLineNumber(), args));
                        annotations.set(i, null);

                    } catch (TermException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            Program rval = new Program(name, state.root.getURL(), statements, annotations, node);

            // remove skips $label:
            ProgramChanger changer = new ProgramChanger(rval, state.env);
            for (int i = 0; i < changer.getProgramLength();) {
                String meta = changer.getAnnotationAt(i);
                if (meta != null && meta.startsWith("$label:") && changer.getStatementAt(i) instanceof SkipStatement)
                    changer.deleteAt(i);
                else
                    i++;
            }

            state.env.addProgram(rval = changer.makeProgram(rval.getName()));
            return rval;

        } catch (EnvironmentException e) {
            e.printStackTrace();

            throw new EnvironmentCreationException("Program creation failed:\n" + e.getMessage());

        } catch (TermException e) {
            e.printStackTrace();

            throw new EnvironmentCreationException("Program creation failed:\n" + e.getMessage());
        }
    }

    public void create(EnvironmentCreationState state) throws EnvironmentCreationException {

        // ensure global and local variable declarations are processed early
        new PreDefinitionVisitor(state);

        // fill environment with information and declarations/implementations
        ProgramMaker pm = new ProgramMaker(state);

        // create programs out of statement triples
        for (ProcedureDeclaration decl : declarations.keySet()) {
            if (!decl.isImplemented())
                continue;

            StatementTripel tripel = declarations.get(decl);

            // assemble statements and annotations
            List<Statement> statements = new LinkedList<Statement>();
            statements.addAll(pm.whereStatements);
            statements.addAll(tripel.preStatements);
            statements.addAll(tripel.bodyStatements);
            statements.addAll(tripel.postStatements);

            List<String> annotations = new LinkedList<String>();
            annotations.addAll(pm.whereAnnotations);
            annotations.addAll(tripel.preAnnotations);
            annotations.addAll(tripel.bodyAnnotations);
            annotations.addAll(tripel.postAnnotations);

            problematicPrograms.add(registerProgram(state, statements, annotations, "procedure_" + decl.getName(),
                    decl, statements.size() - tripel.postStatements.size()));
        }
        for (ProcedureImplementation decl : implementations.keySet()) {
            StatementTripel tripel = implementations.get(decl);

            StatementTripel contract = null;
            for (ProcedureDeclaration d : declarations.keySet()) {
                if (d.getName().equals(decl.getName())) {
                    contract = declarations.get(d);
                    break;
                }
            }
            assert null != contract : "undeclared procedure implementation, should be checked earlier";

            // add contract from declaration
            tripel.preAnnotations.addAll(contract.preAnnotations);
            tripel.postAnnotations.addAll(contract.postAnnotations);
            tripel.preStatements.addAll(contract.preStatements);
            tripel.postStatements.addAll(contract.postStatements);

            // assemble statements and annotations
            List<Statement> statements = new LinkedList<Statement>();
            statements.addAll(pm.whereStatements);
            statements.addAll(tripel.preStatements);
            statements.addAll(tripel.bodyStatements);
            statements.addAll(tripel.postStatements);

            List<String> annotations = new LinkedList<String>();
            annotations.addAll(pm.whereAnnotations);
            annotations.addAll(tripel.preAnnotations);
            annotations.addAll(tripel.bodyAnnotations);
            annotations.addAll(tripel.postAnnotations);

            problematicPrograms.add(registerProgram(state, statements, annotations,
                    "implementation" + decl.getImplementationID() + "_" + decl.getName(), decl, statements.size()
                            - tripel.postStatements.size()));
        }

        // create a problem out of all programs defined in the environment
        for (Program program : problematicPrograms) {
            try {
                if (problem == null) {
                    problem = new LiteralProgramTerm(0, false, program);

                } else {
                    Term[] args = new Term[2];
                    args[0] = problem;
                    args[1] = new LiteralProgramTerm(0, false, program);

                    problem = new Application(state.env.getFunction("$and"), Environment.getBoolType(), args);
                }

            } catch (TermException e) {
                e.printStackTrace();
            }
        }
        // the boogie file does not contain implemented procedures and is
        // thus trivially valid
        if (null == problem)
            problem = Environment.getTrue();
    }

    final Term getProblem() {
        return problem;
    }
}
